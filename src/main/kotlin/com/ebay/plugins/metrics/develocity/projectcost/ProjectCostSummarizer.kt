package com.ebay.plugins.metrics.develocity.projectcost

import com.ebay.plugins.metrics.develocity.MetricSummarizer
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.BuildModelName
import com.gabrielfeo.develocity.api.model.GradleBuildCachePerformanceTaskExecutionEntry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.provider.ListProperty
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * [MetricSummarizer] implementation which evaluates the build-time cost of each project
 * module.
 *
 * NOTE: Because the data model includes many repetitions of the same data (e.g., build scan IDs and user IDs)
 * it is compressed with GZIP into a binary format to reduce the size of the resulting output files.
 */
class ProjectCostSummarizer(
    private val taskNamesExcludedForExecutionDetermination: ListProperty<String>? = null,
    private val compressOutput: Boolean = true, // For testing, manually set to false to see the rendered JSON
): MetricSummarizer<ProjectCostSummary>() {
    override val id = ID
    override val modelsNeeded = setOf(
        BuildModelName.gradleAttributes,
        BuildModelName.gradleBuildCachePerformance,
    )

    private val serializer by lazy {
        ProjectCostSummary.serializer()
    }

    @OptIn(ExperimentalSerializationApi::class) // decodeFromStream
    override fun read(file: File): ProjectCostSummary {
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
            ProjectCostSummary()
        }
    }

    @OptIn(ExperimentalSerializationApi::class) // encodeToStream
    override fun write(intermediate: ProjectCostSummary, file: File) {
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

    override fun extract(build: Build): ProjectCostSummary {
        val projectPathToData = mutableMapOf<String, ProjectData>()
        val projectsWhichDidWork = mutableSetOf<String>()
        val excludedTaskNames = taskNamesExcludedForExecutionDetermination?.get()?.toSet().orEmpty()
        build.models?.gradleBuildCachePerformance?.model?.taskExecution?.forEach { taskExec ->
            with(taskExec) {
                val projectPath = taskPath.substringBeforeLast(":").ifEmpty { ":" }
                val taskName = taskPath.substringAfterLast(":")
                projectPathToData.compute(projectPath) { _, existing ->
                    val didWork = when (avoidanceOutcome) {
                        GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcome.executedNotCacheable,
                        GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcome.executedUnknownCacheability,
                        GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcome.executedCacheable -> {
                            if (excludedTaskNames.contains(taskName)) {
                                0L
                            } else {
                                1L
                            }
                        }
                        else -> 0L
                    }
                    if (didWork != 0L) {
                        projectsWhichDidWork.add(projectPath)
                    }
                    val update = ProjectData(
                        includedBuildCount = if (existing == null) 1L else 0L,
                        buildDuration = duration,
                        taskNameToData = mapOf(taskName to TaskData(1L, didWork, duration)),
                        taskTypeToData = mapOf(taskType to TaskData(1L, didWork, duration)),
                    )
                    existing?.plus(update) ?: update
                }
            }
        }
        val userName = build.models?.gradleAttributes?.model?.environment?.username
        projectsWhichDidWork.forEach { projectPath ->
            projectPathToData.compute(projectPath) { _, existing ->
                existing?.copy(
                    buildsWithExecution = existing.buildsWithExecution + build.id,
                    impactedUsers = if (userName.isNullOrBlank()) {
                        existing.impactedUsers
                    } else {
                        existing.impactedUsers + userName
                    }
                )
            }
        }
        return ProjectCostSummary(
            projectData = projectPathToData,
            totalBuildCount = 1L,
            totalBuildTime = build.models?.gradleBuildCachePerformance?.model?.buildTime ?: 0L,
        )
    }

    override fun reduce(left: ProjectCostSummary, right: ProjectCostSummary): ProjectCostSummary {
        return ProjectCostSummary(
            projectData = left.projectData.merge(right.projectData) { a, b -> a.plus(b) },
            totalBuildCount = left.totalBuildCount + right.totalBuildCount,
            totalBuildTime = left.totalBuildTime + right.totalBuildTime,
        )
    }

    companion object {
        const val ID = "projectCost"

        @OptIn(ExperimentalSerializationApi::class)
        val prettyJson = Json { // this returns the JsonBuilder
            prettyPrint = true
            prettyPrintIndent = " "
        }
    }
}