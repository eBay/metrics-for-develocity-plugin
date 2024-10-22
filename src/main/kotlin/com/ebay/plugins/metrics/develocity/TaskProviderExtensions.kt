@file:Suppress("unused") // Public API

package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.SUMMARIZER_ALL
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.SUMMARIZER_ATTRIBUTE
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.TIME_SPEC_ATTRIBUTE
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

/**
 * Helper function to configure a task's inputs to use the outputs of of metric summarizer,
 * with the summary data spanning the datetime specified.
 */
fun TaskProvider<out MetricSummarizerTask>.inputsFromDateTime(
    project: Project,
    dateTimeSpec: String,
    summarizerId: String,
) = configureInputs(project, dateTimeSpec, summarizerId)

/**
 * Helper function to configure a task's inputs to use the outputs of of metric summarizer,
 * with the summary data spanning the duration specified.
 */
fun TaskProvider<out MetricSummarizerTask>.inputsFromDuration(
    project: Project,
    durationSpec: String,
    summarizerId: String,
) = configureInputs(project, durationSpec, summarizerId)

/**
 * Common helper configuration for tasks which consume the summarizer output.
 */
private fun TaskProvider<out MetricSummarizerTask>.configureInputs(
    project: Project,
    timeSpec: String,
    summarizerId: String,
) {
    val resolveId = "$name-resolve-$summarizerId"
    val existingConfig = project.configurations.findByName(resolveId)
    val configProvider: Provider<Configuration> = if (existingConfig == null) {
        val resolveConfig = project.configurations.register(resolveId)
        resolveConfig.configure { config ->
            with(config) {
                isTransitive = false
                isCanBeResolved = true
                isCanBeConsumed = false
                attributes.attribute(TIME_SPEC_ATTRIBUTE, timeSpec)
                attributes.attribute(SUMMARIZER_ATTRIBUTE, SUMMARIZER_ALL)
                dependencies.add(project.dependencies.project(mapOf("path" to ":")))
            }
        }
        resolveConfig
    } else {
        project.provider { existingConfig }
    }

    configure { self ->
        with(self) {
            dependsOn(configProvider)
            val summaryFileProvider = configProvider.get().incoming.files.elements.map { files ->
                files.map { it.asFile }.firstOrNull()?.resolve(summarizerId)
                    ?: throw GradleException("Could not find summarizer output file '${summarizerId}'")
            }
            summarizerDataProperty.set(summaryFileProvider)
        }
    }
}