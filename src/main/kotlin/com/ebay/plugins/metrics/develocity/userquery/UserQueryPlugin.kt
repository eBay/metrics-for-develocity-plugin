package com.ebay.plugins.metrics.develocity.userquery

import com.ebay.plugins.metrics.develocity.MetricsForDevelocityExtension
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityPlugin
import com.ebay.plugins.metrics.develocity.inputsFromDuration
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This plugin adds a summarizer and a report task that gathers a list of users which performed builds
 * matching the [com.ebay.plugins.metrics.develocity.MetricsForDevelocityPlugin] query filter.
 */
internal class UserQueryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.parent == null) {
            project.plugins.withType(MetricsForDevelocityPlugin::class.java) {
                project.extensions.getByType(MetricsForDevelocityExtension::class.java).apply {
                    summarizers.add(UserQuerySummarizer())
                }
            }

            project.tasks.addRule(
                "Pattern: $TASK_PREFIX-<Java Duration String>  " +
                        "Creates a report showing all the users who performed builds which matched the specified " +
                        "develocity query filter."
            ) { taskName ->
                val matcher = TASK_PATTERN.matcher(taskName)
                if (!matcher.matches()) return@addRule

                val durationStr: String = matcher.group(1)
                project.tasks.register(taskName, UserQueryReportTask::class.java).also { taskProvider ->
                    taskProvider.configure { task ->
                        with(task) {
                            reportFile.set(project.layout.buildDirectory.file("reports/userQuery/userQuery-$durationStr.txt"))
                        }
                    }
                    project.plugins.withType(MetricsForDevelocityPlugin::class.java) {
                        taskProvider.inputsFromDuration(project, durationStr,
                            UserQuerySummarizer.ID
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val TASK_PREFIX = "userQueryReport"
        private val TASK_PATTERN = Regex(
            // Examples:
            //      userQueryReport-P7D
            //      userQueryReport-PT8H
            //      userQueryReport-P2DT8H
            "^\\Q$TASK_PREFIX-\\E(\\w+)$"
        ).toPattern()
    }
}
