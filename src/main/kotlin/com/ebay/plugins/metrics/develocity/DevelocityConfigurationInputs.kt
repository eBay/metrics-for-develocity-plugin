package com.ebay.plugins.metrics.develocity

import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * Interface which may be added to a task to have it be auto-configured with the Develocity
 * server configuration as applied to the root project in the [MetricsForDevelocityExtension].
 *
 * NOTE: Access to the Develocity access key is intentionally not provided here, as it is
 * would potentially be exposed due to its use as part of the czche key.
 */
interface DevelocityConfigurationInputs : Task {
    /**
     * The Develocity server URL.  If the Gradle Develocity or Gradle Enterprise plugins are
     * applied, this will be auto-configured by using the values applied to their respective
     * extensions.
     */
    @get:Input
    val develocityServerUrl: Property<String>
}
