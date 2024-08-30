package com.ebay.plugins.metrics.develocity.userquery

import com.ebay.plugins.metrics.develocity.MetricSummarizerTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Create a report of all users who had builds matching the current filter.
 */
internal abstract class UserQueryReportTask : DefaultTask(), MetricSummarizerTask {
    /**
     * The output directory where the summarizer results should be stored.
     */
    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    init {
        group = "graph analytics"
    }

    @TaskAction
    fun createReport() {
        val summaryFile = summarizerDataProperty.get()
        val model = UserQuerySummarizer().read(summaryFile)
        val report = model.users.sorted().joinToString(
            prefix = "User(s) who have performed builds matching the query filter (${model.users.size}):\n    ",
            separator = "\n    ")
        reportFile.asFile.get().also { reportFile ->
            logger.lifecycle("User report available at: file://${reportFile.absolutePath}")
            reportFile.writeText(report)
        }
    }
}