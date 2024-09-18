package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.service.DevelocityBuildService
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Helper class to made it easier for the plugin to pass these individual items all at once, as parameters,
 * into internal function calls.
 */
internal data class PluginContext(
    val project: Project,
    val buildServiceProvider: Provider<DevelocityBuildService>,
    val currentDayWithHourProvider: Provider<String>,
    val dateHelper: DateHelper,
    val ext: MetricsForDevelocityExtension,
)