package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.DEVELOCITY_SERVER_URL_PROPERTY
import com.ebay.plugins.metrics.develocity.MetricsForDevelocityConstants.SUPPORTED_CONFIGURATION_PROPERTIES_AUTO
import com.ebay.plugins.metrics.develocity.NameUtil.DATETIME_SUFFIX_PATTERN
import com.ebay.plugins.metrics.develocity.NameUtil.DURATION_SUFFIX_PATTERN
import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.initialization.Settings

/**
 * Settings Plugin implementation which applies the Metrics for Develocity plugin to
 * the project.
 *
 * This plugin does the following:
 * - Adds the project plugin to all projects in the build
 * - Transfers the configuration of the Develocity (or Gradle Enterprise) plugin to the
 *   Metrics for Develocity plugin's extension as a conventional default, when applied.
 * - Scans requested task names for time specifications and passes the gathered information
 *   to the project plugin.
 */
@Suppress("unused") // false positive
internal class MetricsForDevelocitySettingsPlugin : MetricsForDevelocityPlugin<Settings> {

    override fun apply(settings: Settings) {
        settings.gradle.lifecycle.beforeProject { project ->
            project.plugins.apply(MetricsForDevelocityProjectPlugin::class.java)
        }

        // If we are using the gradle property to configure the develocity URL, pass this
        // info into the tasks that may consume it.  This is used in preference to the
        // value configured by the develocity or gradle enterprise plugins, when applied.
        settings.providers.gradleProperty(DEVELOCITY_SERVER_URL_PROPERTY).orNull?.let { url ->
            settings.gradle.beforeProject { project ->
                project.tasks.withType(DevelocityConfigurationInputs::class.java) { task ->
                    // `set` instead of `convention` since specification by property value should
                    // take precedence over the default value.
                    task.develocityServerUrl.set(url)
                }
            }
        }

        // Auto-configure the Gradle Enterprise access if the plugin is applied and has been
        // directly configured with a server URL and/or access key.
        settings.plugins.withId("com.gradle.enterprise") {
            // The Develocity plugin is also registered under this ID so we need to avoid running
            // this logic when this is the case.
            if (settings.plugins.hasPlugin("com.gradle.develocity")) {
                return@withId
            }

            @Suppress("DEPRECATION") // GradleEnterpriseExtension is deprecated
            val gradleExt = settings.extensions.getByType(com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension::class.java)
            settings.gradle.afterProject { project ->
                project.plugins.withId("com.ebay.metrics-for-develocity") {
                    project.extensions.findByType(MetricsForDevelocityExtension::class.java)?.let { ext ->
                        with(ext) {
                            develocityServerUrl.convention(gradleExt.server)
                            develocityAccessKey.convention(gradleExt.accessKey)
                        }
                    }
                }
                // Configure tasks wanting to consume the Gradle Enterprise configuration:
                project.tasks.withType(DevelocityConfigurationInputs::class.java).configureEach { task ->
                    // `convention` to allow for possible override by the property value
                    task.develocityServerUrl.convention(gradleExt.server)
                }
            }
        }
        settings.plugins.withId("com.gradle.develocity") {
            val gradleExt = settings.extensions.getByType(DevelocityConfiguration::class.java)
            settings.gradle.afterProject { project ->
                project.plugins.withId("com.ebay.metrics-for-develocity") {
                    project.extensions.findByType(MetricsForDevelocityExtension::class.java)?.let { ext ->
                        with(ext) {
                            develocityServerUrl.convention(gradleExt.server)
                            develocityAccessKey.convention(gradleExt.accessKey)
                        }
                    }
                }
                // Configure tasks wanting to consume the Develocity configuration:
                project.tasks.withType(DevelocityConfigurationInputs::class.java).configureEach { task ->
                    // `convention` to allow for possible override by the property value
                    task.develocityServerUrl.convention(gradleExt.server)
                }
            }
        }

        // Look for task names that have a datetime or duration suffix and feed those into a property that the
        // plugin can consume in order to pro-actively create the consumable configurations.
        settings.gradle.lifecycle.beforeProject { project ->
            if (project.parent == null) {
                val requestedTimeSpecs = mutableListOf<String>()
                project.gradle.startParameter.taskRequests.forEach { taskRequest ->
                    taskRequest.args.forEach { taskName ->
                        DATETIME_SUFFIX_PATTERN.matchEntire(taskName)?.let { matchResult ->
                            matchResult.groups[1]?.value?.let {
                                requestedTimeSpecs.add(it)
                            }
                        }
                        DURATION_SUFFIX_PATTERN.matchEntire(taskName)?.let { matchResult ->
                            matchResult.groups[1]?.value?.let {
                                requestedTimeSpecs.add(it)
                            }
                        }
                    }
                }
                val autoProperties = requestedTimeSpecs.joinToString(separator = ",")
                project.extensions.extraProperties.set(SUPPORTED_CONFIGURATION_PROPERTIES_AUTO, autoProperties)
            }
        }
    }
}
