package com.ebay.plugins.metrics.develocity

import org.gradle.api.attributes.Attribute

/**
 * Constants defined by the plugin which may be used by consuming plugins or build scripts.
 */
object MetricsForDevelocityConstants {
    /**
     * The name that the [MetricsForDevelocityExtension] is registered under.
     */
    const val EXTENSION_NAME = "metricsForDevelocity"

    /**
     * The gradle property name used to configure the query filter.
     */
    const val QUERY_FILTER_PROPERTY = "metricsForDevelocityQueryFilter"

    /**
     * Gradle property which can be used to configure the Develocity server URL.
     */
    const val DEVELOCITY_SERVER_URL_PROPERTY = "metricsForDevelocityServerUrl"

    /**
     * Workaround for Gradle API issue where configuration rules are not applied dynamically as the
     * API appears it should (xref: https://github.com/gradle/gradle/issues/30831).  This
     * property allows for the pre-creation of configurations that will be needed for consumption
     * in order to avoid the bug, which seems to only apply to configurations created via
     * rule definition.
     */
    const val SUPPORTED_CONFIGURATION_PROPERTIES = "metricsForDevelocityConfigurations"

    /**
     * Property name used by the settings plugin to auto-detect the configurations that should
     * be supported.  This works by inspecting the task names of the requested tasks, looking
     * for task names that appear to have a time specification suffix.
     */
    internal const val SUPPORTED_CONFIGURATION_PROPERTIES_AUTO = "metricsForDevelocityAutomaticConfigurations"

    /**
     * The variant attribute used to identify the time specification used to generate the
     * summarizer data.  This is used to disambiguate the configuration when multiple
     * instances are registered.
     */
    val TIME_SPEC_ATTRIBUTE: Attribute<String> = Attribute.of(
        "com.ebay.metrics-for-develocity.time_spec", String::class.java
    )

    /**
     * The variant attribute used to identify what summarizer data is being exported or resolved.
     * The special value of [SUMMARIZER_ALL] is used will result in a directory containing all
     * summarizer data.  All other values should be considered reserved at this time.
     */
    val SUMMARIZER_ATTRIBUTE: Attribute<String> = Attribute.of(
        "com.ebay.metrics-for-develocity.summarizer", String::class.java
    )

    /**
     * The attribute value used to identify the configuration used to export all summarizer data to
     * consuming projects.
     */
    const val SUMMARIZER_ALL = "_all_"
}
