package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.service.DevelocityService
import com.ebay.plugins.metrics.develocity.service.model.BuildQuery
import com.ebay.plugins.metrics.develocity.service.model.BuildsQuery
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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
import javax.inject.Inject

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
    override val summarizersProperty: ListProperty<MetricSummarizer<*>> = objectFactory.listProperty(MetricSummarizer::class.java)

    @get:OutputDirectory
    override val outputDirectoryProperty: DirectoryProperty = objectFactory.directoryProperty()

    @TaskAction
    fun gather() {
        val zone = ZoneId.of(zoneIdProperty.get())
        val startPretty = OffsetDateTime.ofInstant(Instant.ofEpochMilli(startProperty.get()), zone)
        val endPretty = OffsetDateTime.ofInstant(Instant.ofEpochMilli(endExclusiveProperty.get()).minusMillis(1), zone)
        val service = develocityServiceProperty.get()
        val summarizers = summarizersProperty.get()

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

        // Fail task execution if we receive any error responses
        val errorHandler: (response: HttpResponse) -> Unit = {
            runBlocking {
                throw GradleException("Failed to get builds: ${it.call.response.status}\n\t${it.call.response.bodyAsText()}")
            }
        }

        // Determine the complete set of models that the summarizers will need
        val modelsNeeded = summarizers
            .flatMap { it.modelsNeeded }
            .toSet()
            .toList()
            .takeIf { it.isNotEmpty() }

        runBlocking(Dispatchers.Default) {
            // First, query for all the builds but don't return any details
            val buildsQuery = BuildsQuery(
                fromInstant = startProperty.get(),
                maxBuilds = 1000,
                query = queryString,
            )
            service.builds(buildsQuery, errorHandler)
            val buildRefs = service.builds(buildsQuery, errorHandler).toList().also {
                if (it.size == 1000) {
                    throw GradleException("Too many builds! Need to implement paging-based querying!")
                }
            }

            // Initialize our summarizers
            val summarizerStates = summarizers.map { MetricSummarizerState(it) }

            buildRefs
                .map { buildRef ->
                    // Process each build in parallel
                    launch {
                        val detailsQuery = BuildQuery(
                            models = modelsNeeded
                        )
                        // NOTE: The concurrency here is limited by the [DevelocityService]
                        // implementation and is how we control memory utilization.
                        service.build(buildRef.id, detailsQuery, errorHandler) { build ->
                            summarizerStates.forEach { state ->
                                state.ingestBuild(build)
                            }
                        }
                    }
                }
                .map {
                    // Wait for all jobs to complete
                    it.join()
                }

            // Write the reduced results to disk
            val outputDirectory = outputDirectoryProperty.get()
            summarizerStates.forEach { state ->
                val outputFile = PathUtil.hourlySummarizerOutputFile(outputDirectory, state.summarizer)
                state.write(outputFile)
            }
        }
    }
}