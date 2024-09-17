package com.ebay.plugins.metrics.develocity

import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File

/**
 * Task properties which must exist on tasks which consume the summarizer output.
 */
interface MetricSummarizerTask : Task {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    val summarizerDataProperty: Property<File>
}