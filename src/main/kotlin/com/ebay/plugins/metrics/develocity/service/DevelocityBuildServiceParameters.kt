package com.ebay.plugins.metrics.develocity.service

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service parameters for the [DevelocityBuildService].
 */
abstract class DevelocityBuildServiceParameters : BuildServiceParameters {
    /**
     * The maximum number of concurrent requests to make to the Develocity API.  Since these
     * data models are large, higher levels of concurrency will have an impact on memory
     * pressude.
     */
    abstract val maxConcurrency : Property<Int>

    /**
     * The develocity server URL.
     */
    abstract val serverUrlProperty : Property<String>

    /**
     * Develocity access key file from the user's gradle home directory.  This is the location
     * where the automatically provisioned key will be stored, as the the documentation:
     * https://docs.gradle.com/develocity/gradle-plugin/current/#automated_access_key_provisioning
     *
     * It may also be manually provisioned via this file:
     * https://docs.gradle.com/develocity/gradle-plugin/current/#via_file
     */
    abstract val accessKeyFileProperty : RegularFileProperty

    /**
     * Gradle Enterprise access key file from the user's gradle home directory. This is the older
     * location prior to the develocity rename.
     */
    abstract val legacyAccessKeyFileProperty : RegularFileProperty

    /**
     * Develocity access key, when directly configured/specified via the Develocity plugin:
     * https://docs.gradle.com/develocity/gradle-plugin/current/#via_settings_file
     */
    abstract val accessKeyProperty : Property<String>
}