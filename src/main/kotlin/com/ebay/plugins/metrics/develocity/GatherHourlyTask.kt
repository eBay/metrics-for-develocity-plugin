package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.service.DevelocityService
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.BuildModelName
import com.gabrielfeo.develocity.api.model.BuildQuery
import com.gabrielfeo.develocity.api.model.BuildsQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

/**
 * Task implementation which queries Develocity to gather build data for a single hour.  The data
 * is then processed by the metric summarizers and aggregated into a single file per summarizer.
 */
@CacheableTask
open class GatherHourlyTask @Inject constructor(
    objectFactory: ObjectFactory,
): DefaultTask(), MetricsIntermediateTask {
    /**
     * The start time of the hour to gather data for, in milliseconds since the epoch.
     */
    @get:Input
    val startProperty: Property<Long> = objectFactory.property(Long::class.java)

    /**
     * The end time of the hour to gather data for, in milliseconds since the epoch.  All build
     * data to process will have completed before this time.
     */
    @get:Input
    val endExclusiveProperty: Property<Long> = objectFactory.property(Long::class.java)

    /**
     * The reference time zone for use when converting instants into times.
     */
    @get:Input
    val zoneIdProperty: Property<String> = objectFactory.property(String::class.java)

    /**
     * An additional query filter to apply to the Develocity query to narrow the scope of the
     * builds to be processed.  This filter is expressed using the Develocity's advanced search
     * syntax: https://docs.gradle.com/enterprise/api-manual/#advanced_search_syntax
     */
    @get:Input
    @get:Optional
    val queryProperty: Property<String> = objectFactory.property(String::class.java)

    /**
     * Develocity build service instance.
     */
    @get:Internal
    val develocityServiceProperty: Property<DevelocityService> = objectFactory.property(
        DevelocityService::class.java)

    @get:Internal
    val maxConcurrencyProperty: Property<Int> = objectFactory.property(Int::class.java)

    @get:Internal
    override val summarizersProperty: ListProperty<MetricSummarizer<*>> = objectFactory.listProperty(MetricSummarizer::class.java)

    @get:Internal
    val buildScanRetrievalTimeoutProperty: Property<Int> = objectFactory.property(Int::class.java)

    @get:OutputDirectory
    override val outputDirectoryProperty: DirectoryProperty = objectFactory.directoryProperty()

    private val refCount = AtomicInteger()
    private val processedCount = AtomicInteger()
    private val processingBuilds = CopyOnWriteArrayList<String>()

    @TaskAction
    fun gather() {
        val summarizers = summarizersProperty.get()

        runBlocking(Dispatchers.Default) {
            // Initialize our summarizers
            val summarizerStates = summarizers.map { MetricSummarizerState(it) }

            // Determine the complete set of models that the summarizers will need
            val modelsNeeded = summarizers
                .flatMap { it.modelsNeeded }
                .toSet()
                .toList()
                .takeIf { it.isNotEmpty() }
            require(modelsNeeded != null) {
                "No models needed by the summarizers"
            }

            // Create a pipeline to query and process the builds
            val startTime = System.currentTimeMillis()
            val channel = Channel<Build>(capacity = maxConcurrencyProperty.get() * 2)
            val statsJob = logPeriodicStats(startTime)
            produceBuildRefs(channel)
            (1..maxConcurrencyProperty.get()).map {
                consumeBuildRef(channel, modelsNeeded, summarizerStates)
            }.forEach { it.join() }
            statsJob.cancel()
            logCurrentStats(startTime)

            // Write the reduced results to disk
            val outputDirectory = outputDirectoryProperty.get()
            summarizerStates.forEach { state ->
                val outputFile = PathUtil.hourlySummarizerOutputFile(outputDirectory, state.summarizer)
                state.write(outputFile.asFile)
            }
        }
    }

    /**
     * Launches a coroutine which queries for all builds satisfying the query filter and
     * time bounds, sending the resulting [Build] objects to the provided channel.
     */
    private fun CoroutineScope.produceBuildRefs(channel: Channel<Build>) {
        launch {
            val zone = ZoneId.of(zoneIdProperty.get())
            val startPretty =
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(startProperty.get()), zone)
            val endPretty = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(endExclusiveProperty.get()).minusMillis(1), zone
            )

            // Build an advanced syntax filter query
            val dateFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of(ZoneOffset.UTC.id))
            val fromFormatted = dateFormatter.format(startPretty)
            val toFormatted = dateFormatter.format(endPretty)
            val timeQuery = "buildStartTime:[$fromFormatted to $toFormatted]"
            val queryFilter = queryProperty.get()
            val queryString = if (queryFilter.isNullOrBlank()) {
                timeQuery
            } else {
                "$timeQuery and ($queryFilter)"
            }

            val buildsQuery = BuildsQuery(
                fromInstant = startProperty.get(),
                query = queryString,
            )
            develocityServiceProperty.get().builds(buildsQuery).collect {
                refCount.incrementAndGet()
                channel.send(it)
            }
            channel.close()
        }
    }

    /**
     * Launches a coroutine to consume [Build] objects from the provided channel, querying
     * for full build details and processing them, updating the state of the summarizers.
     */
    private fun CoroutineScope.consumeBuildRef(
        channel: ReceiveChannel<Build>,
        modelsNeeded: List<BuildModelName>,
        summarizerStates: List<MetricSummarizerState<*>>,
    ): Job {
        return launch {
            val detailsQuery = BuildQuery(
                models = modelsNeeded
            )
            val timeout = buildScanRetrievalTimeoutProperty.get().seconds
            for (buildRef in channel) {
                processingBuilds.add(buildRef.id)
                val build = withTimeoutOrNull(timeout) {
                    develocityServiceProperty.get().build(buildRef.id, detailsQuery)
                }
                if (build == null) {
                    logger.warn("Unable to process build ${buildRef.id}!  Skipping!")
                } else {
                    summarizerStates.forEach { state ->
                        state.ingestBuild(build)
                    }
                }
                processingBuilds.remove(buildRef.id)
                processedCount.incrementAndGet()
            }
        }
    }

    /**
     * Launches a coroutine to periodically log the current processing statistics.
     */
    private fun CoroutineScope.logPeriodicStats(startTime: Long): Job {
        return launch {
            while (isActive) {
                delay(10.seconds)
                logCurrentStats(startTime)
            }
        }
    }

    /**
     * Logs the current processing statistics.
     */
    private fun logCurrentStats(startTime: Long) {
        val processedSoFar = processedCount.get()
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
        val rate = if (elapsedSeconds == 0.0) {
            "Rate N/A"
        } else {
            "%.2f / second".format(processedSoFar / elapsedSeconds)
        }
        val processingBuilds = processingBuilds.toArray().let { processing ->
            if (processing.isEmpty()) {
                ""
            } else {
                processing.joinToString(
                    prefix = " (Processing ${processing.size} build(s): ",
                    separator = ", ",
                    postfix = ")"
                )
            }

        }
        logger.info("Processed $processedSoFar build(s) in ${elapsedSeconds.roundToInt()} second(s): $rate $processingBuilds")
    }
}
