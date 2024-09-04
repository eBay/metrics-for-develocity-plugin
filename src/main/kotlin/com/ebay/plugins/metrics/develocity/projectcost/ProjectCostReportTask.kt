package com.ebay.plugins.metrics.develocity.projectcost

import com.ebay.plugins.metrics.develocity.MetricSummarizerTask
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Project cost report task which consumes the project cost summary data and generates a final
 * data model.
 */
@CacheableTask
internal abstract class ProjectCostReportTask : DefaultTask(), MetricSummarizerTask {
    /**
     * The output directory where the summarizer results should be stored.
     */
    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun createReport() {
        val projectCostReportFile = summarizerDataProperty.get()
        val model = ProjectCostSummarizer().read(projectCostReportFile)
        val projectModuleCostReports = model.projectData.mapValues { (_, summary) ->
            val executedBuildCount = summary.buildsWithExecution.size
            val executedTaskCount = summary.taskNameToData.values.map { it.executed }.reduce { acc, it ->
                acc + it
            }
            val executedBuildAvgTasks = executedTaskCount.safeDiv(executedBuildCount.toLong())
            val executedBuildPercentage = (executedBuildCount.toFloat() / model.totalBuildCount.toFloat()) * 100.00F
            val executedBuildAvgDuration = summary.buildDuration.safeDiv(executedBuildCount.toLong())

            ProjectModuleCostReport(
                buildAvgDuration = executedBuildAvgDuration,
                buildAvgTasks = executedBuildAvgTasks,
                buildCostScalar = executedBuildAvgDuration * executedBuildPercentage.toInt(),
                buildCount = executedBuildCount,
                buildDuration = summary.buildDuration,
                buildPercentage = executedBuildPercentage,
                impactedUserCount = summary.impactedUsers.size,
            )
        }
        val report = ProjectCostReport(
            projectData = projectModuleCostReports,
            totalBuildCount = model.totalBuildCount,
            totalBuildTime = model.totalBuildTime
        )
        val reportJson = prettyJson.encodeToString(ProjectCostReport.serializer(), report)
        reportFile.asFile.get().writeText(reportJson)
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val prettyJson = Json { // this returns the JsonBuilder
            prettyPrint = true
            prettyPrintIndent = " "
        }
    }
}