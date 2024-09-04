@file:Suppress("unused") // Public API

package com.ebay.plugins.metrics.develocity

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/**
 * Helper function to configure a task's inputs to use the outputs of of metric summarizer,
 * with the summary data spanning the duration specified.
 */
fun TaskProvider<out MetricSummarizerTask>.inputsFromHourly(
    project: Project,
    timeSpec: String,
    summarizerId: String,
) {
    val inputTask = project.internalExt().createHourlyTask(timeSpec)
    configureInputs(inputTask, summarizerId)
}

/**
 * Helper function to configure a task's inputs to use the outputs of of metric summarizer,
 * with the summary data spanning the duration specified.
 */
fun TaskProvider<out MetricSummarizerTask>.inputsFromDaily(
    project: Project,
    timeSpec: String,
    summarizerId: String,
) {
    val inputTask = project.internalExt().createDailyTask(timeSpec)
    configureInputs(inputTask, summarizerId)
}

/**
 * Helper function to configure a task's inputs to use the outputs of of metric summarizer,
 * with the summary data spanning the duration specified.
 */
fun TaskProvider<out MetricSummarizerTask>.inputsFromDuration(
    project: Project,
    durationSpec: String,
    summarizerId: String,
) {
    val inputTask = project.internalExt().createDurationTask(durationSpec)
    configureInputs(inputTask, summarizerId)
}

/**
 * Get the internal extension for a given project.
 */
private fun Project.internalExt(): MetricsForDevelocityInternalExtension {
    /*
     * NOTE: This is a bit of a project isolation violation.  We could likely avoid this if task
     * rules applied to programmatically created tasks as well as those created on the command
     * line.  This works for now...
     */
    // TODO: This violates project isolation
    val ext = project.rootProject.extensions.getByType(MetricsForDevelocityExtension::class.java)
    return ext.extensions.getByType(MetricsForDevelocityInternalExtension::class.java)
}

/**
 * Common helper configuration for tasks which consume the summarizer output.
 */
private fun TaskProvider<out MetricSummarizerTask>.configureInputs(
    inputTask: TaskProvider<out MetricsIntermediateTask>,
    summarizerId: String,
) {
    configure { self ->
        with(self) {
            dependsOn(inputTask)
            val inputFileProvider = inputTask.flatMap { aggregateTask ->
                PathUtil.summarizerFile(aggregateTask.outputDirectoryProperty, summarizerId)
            }
            summarizerDataProperty.set(inputFileProvider)
        }
    }
}