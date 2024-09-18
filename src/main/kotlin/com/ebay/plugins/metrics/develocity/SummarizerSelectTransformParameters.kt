package com.ebay.plugins.metrics.develocity

import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * Parameters for the summarizer selection transform.
 */
interface SummarizerSelectTransformParameters : TransformParameters {
    /**
     * The [MetricSummarizer.id] of the summarizer data to extract.
     */
    @get:Input
    val summarizerId: Property<String>
}
