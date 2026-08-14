@file:Suppress("unused") // Public API

package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.SUMMARIZER_ALL
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.summarizerCapability
import com.ebay.plugins.metrics.develocity.NameUtil.DATETIME_TASK_PATTERN
import com.ebay.plugins.metrics.develocity.NameUtil.DURATION_TASK_PATTERN
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
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
    // Isolated projects cannot reach another project's extensions. Root is configured
    // first; subprojects rely on that registration (or settings-plugin pre-creation).
    if (project.parent == null) {
        val registered = project.extensions
            .getByType(MetricsForDevelocityExtension::class.java)
            .ensureTimeSpecConfiguration(timeSpec)
        if (!registered) {
            throw GradleException("Unable to parse time spec: $timeSpec\n" +
                    "\tSupported patterns:\n" +
                    "\t\t'${DATETIME_TASK_PATTERN.pattern()}'\n" +
                    "\t\t'${DURATION_TASK_PATTERN.pattern()}' (group 1 parsed as a Java duration)")
        }
    }

    val resolveId = "$name-resolve-$summarizerId"
    val existingConfig = project.configurations.findByName(resolveId)
    val configProvider: Provider<Configuration> = if (existingConfig == null) {
        val resolveConfig = project.configurations.register(resolveId)
        resolveConfig.configure { config ->
            with(config) {
                isTransitive = false
                isCanBeResolved = true
                isCanBeConsumed = false
                val dep = project.dependencies.project(mapOf("path" to ":")) as ModuleDependency
                dep.capabilities { caps ->
                    caps.requireCapability(summarizerCapability(timeSpec, SUMMARIZER_ALL))
                }
                dependencies.add(dep)
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