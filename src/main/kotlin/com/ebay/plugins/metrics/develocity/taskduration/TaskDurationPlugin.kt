package com.ebay.plugins.metrics.develocity.taskduration

import com.ebay.plugins.metrics.develocity.MetricsForDevelocityExtension
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityPlugin
import com.ebay.plugins.metrics.develocity.inputsFromDuration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import java.time.ZoneId
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

/**
 * This plugin adds a summarizer and a report task that aggregates the total execution duration
 * of tasks, identified by either task name (across all modules) or by task type.
 */
internal class TaskDurationPlugin @Inject constructor(
    private val objectFactory: ObjectFactory,
) : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.parent == null) {
            // Local property that we'll later set to the value from the extension
            val localZoneIdProp = objectFactory.property(String::class.java)
                .convention(ZoneId.systemDefault().id)
                .also {
                    it.finalizeValueOnRead()
                }

            // Create a provider of the timezone's short description
            val tzShortName = localZoneIdProp.map {
                val tz = TimeZone.getTimeZone(ZoneId.of(it))
                val daylight = tz.inDaylightTime(Date())
                tz.getDisplayName(daylight, TimeZone.SHORT)
            }

            // Register a single parameter-less summarizer that collects data for all tasks
            project.plugins.withType(MetricsForDevelocityPlugin::class.java) {
                project.extensions.getByType(MetricsForDevelocityExtension::class.java).apply {
                    summarizers.add(TaskDurationSummarizer(zoneIdProvider = zoneId))
                    localZoneIdProp.set(zoneId) // Set the local property from the extension's zone ID
                }
            }

            project.tasks.addRule(
                "Pattern: $TASK_PREFIX-<Task Name>-<Java Duration String>  " +
                        "Creates a report showing the total execution duration of the specified task " +
                        "for builds which matched the specified develocity query filter. "
            ) { taskName ->
                val matcher = TASK_PATTERN.matcher(taskName)
                if (!matcher.matches()) return@addRule

                val targetTaskName = matcher.group(1)
                val durationStr = matcher.group(2)

                // Register the report task
                project.tasks.register(taskName, TaskDurationByNameReportTask::class.java).also { taskProvider ->
                    taskProvider.configure { task ->
                        with(task) {
                            durationProvider.set(durationStr)
                            this.taskName.set(targetTaskName)
                            zoneId.set(localZoneIdProp)

                            val fileNameProvider = project.providers.zip(this.taskName, tzShortName) { name, tz ->
                                "taskNameDuration-$name-$durationStr-$tz.txt"
                            }
                            reportFile.set(project.providers.zip(project.layout.buildDirectory, fileNameProvider) { dir, fileName ->
                                dir.file("reports/taskDuration/$fileName")
                            })
                        }
                    }
                    
                    // Use a single summarizer ID for all tasks
                    project.plugins.withType(MetricsForDevelocityPlugin::class.java) {
                        taskProvider.inputsFromDuration(project, durationStr, TaskDurationSummarizer.ID)
                    }
                }
            }

            project.tasks.addRule(
                "Pattern: $BY_TYPE_PREFIX-<Java Duration String>  " +
                        "Creates a report showing the total execution duration of the specified task " +
                        "type for builds which matched the specified develocity query filter. " +
                        "Use --task-type to specify the task type."
            ) { taskName ->
                val matcher = BY_TYPE_PATTERN.matcher(taskName)
                if (!matcher.matches()) return@addRule

                val durationStr = matcher.group(1)

                // Register the report task
                project.tasks.register(taskName, TaskDurationByTypeReportTask::class.java).also { taskProvider ->
                    taskProvider.configure { task ->
                        with(task) {
                            durationProvider.set(durationStr)
                            zoneId.set(localZoneIdProp)
                            val fileNameProvider = project.providers.zip(taskType, tzShortName) { type, tz ->
                                "taskTypeDuration-$type-$durationStr-$tz.txt"
                            }
                            reportFile.set(project.providers.zip(project.layout.buildDirectory, fileNameProvider) { dir, fileName ->
                                dir.file("reports/taskDuration/$fileName")
                            })
                        }
                    }

                    // Use a single summarizer ID for all tasks
                    project.plugins.withType(MetricsForDevelocityPlugin::class.java) {
                        taskProvider.inputsFromDuration(project, durationStr, TaskDurationSummarizer.ID)
                    }
                }
            }
        }
    }

    companion object {
        private const val TASK_PREFIX = "taskDurationReport"
        private const val BY_TYPE_PREFIX = "taskTypeDurationReport"

        private val TASK_PATTERN = Regex(
            // Examples:
            //      taskDurationReport-compileJava-P7D
            //      taskDurationReport-test-PT8H
            //      taskDurationReport-assemble-P2DT8H
            "^\\Q$TASK_PREFIX-\\E([\\w-]+)-(\\w+)$"
        ).toPattern()

        private val BY_TYPE_PATTERN = Regex(
            // Examples:
            //      taskTypeDurationReport-P7D
            //      taskTypeDurationReport-PT8H
            //      taskTypeDurationReport-P2DT8H
            "^\\Q$BY_TYPE_PREFIX-\\E(\\w+)$"
        ).toPattern()
    }
}
