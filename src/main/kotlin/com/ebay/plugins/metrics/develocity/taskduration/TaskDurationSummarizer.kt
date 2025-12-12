package com.ebay.plugins.metrics.develocity.taskduration

import com.ebay.plugins.metrics.develocity.DateHelper
import com.ebay.plugins.metrics.develocity.MetricSummarizer
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.BuildModelName
import com.gabrielfeo.develocity.api.model.GradleBuildCachePerformanceTaskExecutionEntry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.provider.Property
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.time.Instant
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.time.ExperimentalTime

/**
 * [MetricSummarizer] implementation which aggregates the total execution duration of all tasks.
 * Data is collected by both task name (across all modules) and by fully qualified task type.
 */
class TaskDurationSummarizer(
    private val zoneIdProvider: Property<String>,
    private val compressOutput: Boolean = true,
) : MetricSummarizer<TaskDurationSummary>() {
    override val id = ID

    private val dateHelper by lazy {
        DateHelper(zoneIdProvider)
    }

    override val modelsNeeded = setOf(
        BuildModelName.gradleAttributes,
        BuildModelName.gradleBuildCachePerformance,
    )

    private val serializer by lazy {
        TaskDurationSummary.serializer()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun read(file: File): TaskDurationSummary {
        return if (file.exists()) {
            file.inputStream().use { inputStream ->
                BufferedInputStream(inputStream).use { buffered ->
                    if (compressOutput) {
                        GZIPInputStream(buffered).use { gzip ->
                            Json.decodeFromStream(serializer, gzip)
                        }
                    } else {
                        prettyJson.decodeFromStream(serializer, buffered)
                    }
                }
            }
        } else {
            TaskDurationSummary()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun write(intermediate: TaskDurationSummary, file: File) {
        file.outputStream().use { outputStream ->
            BufferedOutputStream(outputStream).use { buffered ->
                if (compressOutput) {
                    GZIPOutputStream(buffered).use { gzip ->
                        Json.encodeToStream(serializer, intermediate, gzip)
                    }
                } else {
                    prettyJson.encodeToStream(serializer, intermediate, buffered)
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun extract(build: Build): TaskDurationSummary {
        val taskNameData: MutableMap<String, TaskExecutionSummary> = mutableMapOf()
        val taskTypeData: MutableMap<String, TaskExecutionSummary> = mutableMapOf()

        build.models?.gradleBuildCachePerformance?.model?.taskExecution?.forEach { taskExec ->
            val buildStartTime = build.models?.gradleAttributes?.model?.buildStartTime ?: return@forEach

            with(taskExec) {
                // Only process executed tasks
                if (avoidanceOutcome == GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcome.executedNotCacheable ||
                    avoidanceOutcome == GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcome.executedUnknownCacheability ||
                    avoidanceOutcome == GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcome.executedCacheable) {

                    val executionData = TaskExecutionData(executionCount = 1, totalDuration = duration)
                    val instant = Instant.ofEpochMilli(buildStartTime)
                    val dayStartString = dateHelper.toDailyString(instant)
                    val summary = TaskExecutionSummary(
                        total = executionData,
                        byDay = mapOf(dayStartString to executionData)
                    )

                    // Extract task name from path
                    val taskName = taskPath.substringAfterLast(":")

                    taskNameData.merge(taskName, summary) { left, right ->
                        left + right
                    }
                    taskTypeData.merge(taskType, summary) { left, right ->
                        left + right
                    }
                }
            }
        }

        return TaskDurationSummary(taskNameData, taskTypeData)
    }

    override fun reduce(left: TaskDurationSummary, right: TaskDurationSummary): TaskDurationSummary {
        return left + right
    }

    companion object {
        const val ID = "taskDuration"

        @OptIn(ExperimentalSerializationApi::class)
        val prettyJson = Json {
            prettyPrint = true
            prettyPrintIndent = " "
        }
    }
}
