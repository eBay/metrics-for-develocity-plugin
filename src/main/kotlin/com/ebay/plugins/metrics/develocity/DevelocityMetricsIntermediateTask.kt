package com.ebay.plugins.metrics.develocity

import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory

/**
 * Properties which are common to the tasks which gather and process the metric data.
 */
interface DevelocityMetricsIntermediateTask : Task {
    /**
     * List of summarizers to use when processing the metric data.
     */
    @get:Internal
    val summarizersProperty: ListProperty<DevelocityMetricSummarizer<*>>

    /**
     * The output directory where the summarizer results should be stored.
     */
    @get:OutputDirectory
    val outputDirectoryProperty: DirectoryProperty
}