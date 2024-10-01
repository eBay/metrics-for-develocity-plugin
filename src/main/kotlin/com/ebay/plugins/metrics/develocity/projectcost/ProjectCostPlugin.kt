package com.ebay.plugins.metrics.develocity.projectcost

import com.ebay.plugins.graph.analytics.GraphExtension
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityExtension
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityPlugin
import com.ebay.plugins.metrics.develocity.inputsFromDuration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

/**
 * Plugin implementation used to add project build cost reporting functionality leveraging the
 * [MetricsForDevelocityPlugin].
 */
internal class ProjectCostPlugin @Inject constructor(
    private val providerFactory: ProviderFactory
) : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.parent == null) {
            project.plugins.withType(MetricsForDevelocityPlugin::class.java) {
                project.extensions.getByType(MetricsForDevelocityExtension::class.java).apply {
                    val projectCostExtension = project.extensions.create(EXTENSION_NAME, ProjectCostExtension::class.java)
                    projectCostExtension.taskNamesIgnoredForExecutionDetermination.convention(listOf(
                        "checkKotlinGradlePluginConfigurationErrors" // https://youtrack.jetbrains.com/issue/KT-61943
                    ))
                    summarizers.add(ProjectCostSummarizer(projectCostExtension.taskNamesIgnoredForExecutionDetermination))
                }
            }
        }

        val reportTaskProviderFun: (reportDuration: String) -> TaskProvider<ProjectCostReportTask> = { reportDuration ->
            val taskName = "projectCostReport-$reportDuration"
            if (project.tasks.names.contains(taskName)) {
                // task already exists.  Return its TaskProvider.
                project.tasks.named(taskName, ProjectCostReportTask::class.java)
            } else {
                // Task does not exist, so we need to create it.
                project.tasks.register(taskName, ProjectCostReportTask::class.java).also { taskProvider ->
                    taskProvider.configure { task ->
                        with(task) {
                            reportFile.set(project.layout.buildDirectory.file("projectCost/intermediate/$taskName.json"))
                        }
                    }
                    taskProvider.inputsFromDuration(project, reportDuration, ProjectCostSummarizer.ID)
                }
            }
        }

        project.tasks.addRule(
            "Pattern: $PROJECT_COST_TASK_PREFIX-<Java Duration String>  " +
                    "Creates a project-specific report showing details about what has contributed to the overall" +
                    "project build cost."
        ) { taskName ->
            val matcher = PROJECT_COST_TASK_PATTERN.matcher(taskName)
            if (!matcher.matches()) return@addRule

            val durationStr: String = matcher.group(1)
            reportTaskProviderFun.invoke(durationStr)
        }

        project.tasks.addRule(
            "Pattern: $INSPECTION_TASK_PREFIX-<Java Duration String>  " +
                    "Creates a project-specific report showing details about what has contributed " +
                    "to the overall project build cost."
        ) { taskName ->
            val matcher = INSPECTION_TASK_PATTERN.matcher(taskName)
            if (!matcher.matches()) return@addRule

            val durationStr: String = matcher.group(1)
            val taskProvider = project.tasks.register(taskName, ProjectCostInspectionTask::class.java)
            taskProvider.configure { task ->
                with(task) {
                    projectPath.set(project.path)
                    outputFile.set(project.layout.buildDirectory.file("reports/projectCost/inspection.txt"))
                }
            }
            project.plugins.withType(MetricsForDevelocityPlugin::class.java) {
                taskProvider.inputsFromDuration(project, durationStr, ProjectCostSummarizer.ID)
            }
        }

        val projectCostGraphAnalysis = providerFactory.gradleProperty(GRAPH_ANALYSIS_ENABLED_PROPERTY).orNull.toBoolean()
        if (projectCostGraphAnalysis) {
            project.plugins.withId("com.ebay.graph-analytics") {
                val reportDuration = providerFactory.gradleProperty(GRAPH_ANALYSIS_DURATION_PROPERTY).orElse("P7D").get()
                val reportTaskProvider = reportTaskProviderFun.invoke(reportDuration)
                val graphAnalysisTask = project.tasks.register("projectCostGraphAnalysis-$reportDuration", ProjectCostGraphAnalysisTask::class.java)
                graphAnalysisTask.configure { task ->
                    with(task) {
                        projectReportProperty.set(reportTaskProvider.flatMap { it.reportFile })
                    }
                }
                project.extensions.getByType(GraphExtension::class.java).apply {
                    analysisTasks.add(graphAnalysisTask)
                }
            }
        }
    }

    companion object {
        private const val EXTENSION_NAME = "projectCost"
        private const val INSPECTION_TASK_PREFIX = "projectCostInspectionReport"
        private const val PROJECT_COST_TASK_PREFIX = "projectCostReport"
        private const val GRAPH_ANALYSIS_ENABLED_PROPERTY = "projectCostGraphAnalysisEnabled"
        private const val GRAPH_ANALYSIS_DURATION_PROPERTY = "projectCostGraphAnalysisDuration"

        private val INSPECTION_TASK_PATTERN = Regex(
            // Examples:
            //      projectCostInspectionReport-P7D
            //      projectCostInspectionReport-PT8H
            //      projectCostInspectionReport-P2DT8H
            "^\\Q$INSPECTION_TASK_PREFIX-\\E(\\w+)$"
        ).toPattern()

        private val PROJECT_COST_TASK_PATTERN = Regex(
            // Examples:
            //      projectCostReport-P7D
            //      projectCostReport-PT8H
            //      projectCostReport-P2DT8H
            "^\\Q$PROJECT_COST_TASK_PREFIX-\\E(\\w+)$"
        ).toPattern()
    }
}
