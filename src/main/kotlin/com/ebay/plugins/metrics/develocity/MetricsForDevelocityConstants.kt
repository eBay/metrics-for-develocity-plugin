package com.ebay.plugins.metrics.develocity

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
     * The attribute value used to identify the configuration used to export all summarizer data to
     * consuming projects.
     */
    const val SUMMARIZER_ALL = "_all_"

    /**
     * Group ID used in summarizer configuration capabilities.  This is defined by the plugin
     * and is not the consuming project's group.
     */
    const val SUMMARIZER_CAPABILITY_GROUP = "com.ebay.plugins"

    /**
     * Version used in summarizer configuration capabilities.  A fixed value is used so that
     * matching does not depend on `project.version` (often `unspecified`).
     */
    const val SUMMARIZER_CAPABILITY_VERSION = "1.0"

    /**
     * Returns the capability notation for a summarizer configuration variant.
     *
     * Example: `com.ebay.metrics-for-develocity:summarizer-_all_-P2D:1.0`
     */
    fun summarizerCapability(timeSpec: String, summarizer: String = SUMMARIZER_ALL): String {
        return "$SUMMARIZER_CAPABILITY_GROUP:metrics-for-develocity-summarizer-$summarizer-$timeSpec:$SUMMARIZER_CAPABILITY_VERSION"
    }
}
