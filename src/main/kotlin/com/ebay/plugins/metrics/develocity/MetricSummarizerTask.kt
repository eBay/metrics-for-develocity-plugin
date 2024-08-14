package com.ebay.plugins.metrics.develocity

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

/**
 * Task properties which must exist on tasks which consume the summarizer output.
 */
interface MetricSummarizerTask : Task {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    val summarizerDataProperty: RegularFileProperty
}