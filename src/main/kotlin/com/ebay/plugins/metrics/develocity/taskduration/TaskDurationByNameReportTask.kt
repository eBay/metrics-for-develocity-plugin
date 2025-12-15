package com.ebay.plugins.metrics.develocity.taskduration

import com.ebay.plugins.metrics.develocity.MetricSummarizerTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import javax.inject.Inject

/**
 * Create a report of the total execution duration of a specific task, by name.
 */
internal abstract class TaskDurationByNameReportTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask(), MetricSummarizerTask {
    /**
     * The time span / duration being reported on.
     */
    @get:Input
    val durationProvider: Property<String> = objectFactory.property(String::class.java)
    
    /**
     * The name of the task to report on.
     */
    @get:Input
    @get:Option(option = "task-name", description = "Unqualified task name")
    val taskName: Property<String> = objectFactory.property(String::class.java)

    /**
     * The Time Zone ID to use when generating the report timestamps.
     */
    @get:Input
    val zoneId: Property<String> = objectFactory.property(String::class.java)

    /**
     * The output file where the report should be stored.
     */
    @get:OutputFile
    val reportFile: RegularFileProperty = objectFactory.fileProperty()

    @TaskAction
    fun createReport() {
        val summaryFile = summarizerDataProperty.get()
        val summarizer = TaskDurationSummarizer(zoneId)
        val model = summarizer.read(summaryFile)

        val durationStr = durationProvider.get()
        val targetTaskName = taskName.get()
        TaskExecutionReportHelper.writeTaskExecutionReport(
            logger = logger,
            reportTitle = "Task Duration Report for task '$targetTaskName' for the last $durationStr (across all modules)",
            executionSummary = model.taskNameData[targetTaskName],
            reportFile = reportFile,
            timeZoneIdProvider = zoneId,
        )
    }
}
