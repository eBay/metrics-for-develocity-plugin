package com.ebay.plugins.metrics.develocity

import org.gradle.api.Plugin

/**
 * Plugin indirection layer, allowing the plugin ID to be applied to either a project or settings.
 *
 * We use this indirection layer to allow for the plugin's application to be uniformly detected
 * via this class, irrespective of the underlying implementation class.  This allows plugin
 * authors to guard access to the extensions with code such as the following:
 *
 * ```kotlin
 * project.plugins.withType(MetricsForDevelocityPlugin::class.java) { ... }
 * ```
 */
interface MetricsForDevelocityPlugin<T> : Plugin<T>