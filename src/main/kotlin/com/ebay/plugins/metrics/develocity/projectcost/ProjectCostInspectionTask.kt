package com.ebay.plugins.metrics.develocity.projectcost

import com.ebay.plugins.metrics.develocity.DevelocityConfigurationInputs
import com.ebay.plugins.metrics.develocity.MetricSummarizerTask
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to create a textual report for a specific project, detailing the aggregate costs
 * associated with the project.
 */
@CacheableTask
internal abstract class ProjectCostInspectionTask : DefaultTask(), MetricSummarizerTask, DevelocityConfigurationInputs {
    @get:OutputFile
    internal abstract val outputFile: RegularFileProperty

    @get:Input
    internal abstract val projectPath: Property<String>

    @TaskAction
    fun generateReport() {
        val projectSummaryFile = summarizerDataProperty.get()
        val model = ProjectCostSummarizer().read(projectSummaryFile)

        val path = projectPath.get()
        val data = model.projectData[path]
            ?: throw GradleException("Project path '$path' not found in data")
        val topTasksByAvgDuration = data.taskNameToData.asSequence()
            .map { (taskName, taskData) ->
                Pair(taskName, taskData.duration.safeDiv(taskData.executed))
            }.toList().sortedByDescending { it.second }.take(TOP_N).joinToString(
                prefix = "Top $TOP_N tasks by average duration:\n\t",
                separator = "\n\t",
                postfix = "\n\n"
            ) {
                "${it.second} ms -- ${it.first} ms"
            }
        val topTasksByExecutions = data.taskNameToData.asSequence()
            .map { (taskName, taskData) ->
                Pair(taskName, taskData.executed)
            }.toList().sortedByDescending { it.second }.take(TOP_N).joinToString(
                prefix = "Top $TOP_N tasks by execution count:\n\t",
                separator = "\n\t",
                postfix = "\n\n"
            ) {
                "${it.second} -- ${it.first}"
            }
        val topTypesByAvgDuration = data.taskTypeToData.asSequence()
            .map { (taskName, taskData) ->
                Pair(taskName, taskData.duration.safeDiv(taskData.executed))
            }.toList().sortedByDescending { it.second }.take(TOP_N).joinToString(
                prefix = "Top $TOP_N task types by average duration:\n\t",
                separator = "\n\t",
                postfix = "\n\n"
            ) {
                "${it.second} ms -- ${it.first} ms"
            }
        val topTypesByExecutions = data.taskTypeToData.asSequence()
            .map { (taskType, taskData) ->
                Pair(taskType, taskData.executed)
            }.toList().sortedByDescending { it.second }.take(TOP_N).joinToString(
                prefix = "Top $TOP_N task types by execution count:\n\t",
                separator = "\n\t",
                postfix = "\n\n"
            ) {
                "${it.second} -- ${it.first}"
            }
        val impactedUserReport = if (data.impactedUsers.isEmpty()) {
            "No impacted users\n\n"
        } else {
            data.impactedUsers.sorted().joinToString(
                prefix = "Impacted users:\n\t",
                separator = "\n\t",
                postfix = "\n\n")
        }
        val baseUrl = develocityServerUrl.orNull?.let {
            if (it.isEmpty()) {
                ""
            } else if (it.endsWith("/")) {
                it
            } else {
                "$it/s/"
            }
        }
        val projectPathWithDelimiter = projectPath.get().let { if (it == ":") it else "$it:" }
        val buildScansReport = if (data.buildsWithExecution.isEmpty()) {
            "No builds with executed tasks\n\n"
        } else {
            data.buildsWithExecution.joinToString(
                prefix = "Builds with executed tasks:\n\t",
                separator = "\n\t",
                postfix = "\n\n") { buildId ->
                "$baseUrl$buildId/timeline?hide-timeline&name=$projectPathWithDelimiter&outcome=success&sort=longest"
            }
        }

        val report = buildString {
            appendLine("Project: $path")
            appendLine()
            appendLine("Build count: ${data.includedBuildCount}")
            appendLine("Build count (executed tasks only): ${data.buildsWithExecution.size}")
            appendLine("Impacted users: ${data.impactedUsers.size}")
            appendLine("Total aggregate build duration: ${data.buildDuration} ms")
            appendLine()
            append(topTasksByAvgDuration)
            append(topTasksByExecutions)
            append(topTypesByAvgDuration)
            append(topTypesByExecutions)
            append(impactedUserReport)
            append(buildScansReport)
        }.replace("\n", System.lineSeparator())
        outputFile.asFile.get().also { reportFile ->
            logger.lifecycle("Project cost inspection report available at: file://${reportFile.absolutePath}")
            reportFile.writeText(report)
        }
    }

    companion object {
        private const val TOP_N = 25
    }
}
