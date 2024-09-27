@file:Suppress("unused") // Public API

package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.SUMMARIZER_ALL
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.SUMMARIZER_ATTRIBUTE
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.TIME_SPEC_ATTRIBUTE
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
    timeSpec: String,
    summarizerId: String,
) = configureInputs(project, NameUtil.dateTime(timeSpec), summarizerId)

/**
 * Helper function to configure a task's inputs to use the outputs of of metric summarizer,
 * with the summary data spanning the duration specified.
 */
fun TaskProvider<out MetricSummarizerTask>.inputsFromDuration(
    project: Project,
    durationSpec: String,
    summarizerId: String,
) = configureInputs(project, NameUtil.duration(durationSpec), summarizerId)

/**
 * Common helper configuration for tasks which consume the summarizer output.
 */
private fun TaskProvider<out MetricSummarizerTask>.configureInputs(
    project: Project,
    configurationName: String,
    summarizerId: String,
) {
    // NOTE: It appears that `registerTransform` is reentrant so we don't have to worry about multiple invocations
    project.dependencies.registerTransform(SummarizerSelectTransform::class.java) { spec ->
        with(spec) {
            from.attribute(SUMMARIZER_ATTRIBUTE, SUMMARIZER_ALL)
            to.attribute(SUMMARIZER_ATTRIBUTE, summarizerId)
            parameters.summarizerId.set(summarizerId)
        }
    }

    val resolveId = "metricsForDevelocity-$configurationName-resolve"
    val existingConfig = project.configurations.findByName(resolveId)
    val configProvider: Provider<Configuration> = if (existingConfig == null) {
        val resolveConfig = project.configurations.register(resolveId)
        resolveConfig.configure { config ->
            with(config) {
                isTransitive = false
                isCanBeResolved = true
                isCanBeConsumed = false
                attributes.attribute(TIME_SPEC_ATTRIBUTE, configurationName)
                attributes.attribute(SUMMARIZER_ATTRIBUTE, SUMMARIZER_ALL)

                dependencies.add(
                    project.dependencies.project(
                        mapOf(
                            "path" to ":",
                            "configuration" to configurationName,
                        )
                    )
                )
            }
        }
        resolveConfig
    } else {
        project.provider { existingConfig }
    }

    configure { self ->
        with(self) {
            dependsOn(configProvider)
            summarizerDataProperty.set(configProvider.map { cfg ->
                cfg.incoming.artifactView { view ->
                    view.attributes.attribute(SUMMARIZER_ATTRIBUTE, summarizerId)
                }.files.singleFile
            })
        }
    }
}