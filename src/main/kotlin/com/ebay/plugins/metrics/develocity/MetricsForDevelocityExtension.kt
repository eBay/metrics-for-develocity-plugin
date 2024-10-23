package com.ebay.plugins.metrics.develocity

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Gradle extension used to configure the [MetricsForDevelocityPlugin].
 */
abstract class MetricsForDevelocityExtension : ExtensionAware {
    /**
     * The zone ID to use for reporting purposes.  The time zone provided controls the boundaries
     * of what is considered to be the "current day".  By default, assumes the system's time zone.
     */
    abstract val zoneId: Property<String>

    /**
     * Filter to apply to the Develocity query to narrow the scope of the builds to be processed
     * (optional).  This filter is expressed using the Develocity's advanced search syntax:
     * https://docs.gradle.com/enterprise/api-manual/#advanced_search_syntax
     */
    abstract val develocityQueryFilter: Property<String>

    /**
     * The Develocity server URL.  If the Gradle Develocity or Gradle Enterprise plugins are
     * applied, this will be auto-configured by using the values applied to their respective
     * extensions.
     */
    abstract val develocityServerUrl: Property<String>

    /**
     * The Develocity server access key.  If the Gradle Develocity or Gradle Enterprise plugins are
     * applied, this will be auto-configured by using the values applied to their respective
     * extensions.
     */
    abstract val develocityAccessKey: Property<String>

    /**
     * The maximum number of concurrent requests to make to the Develocity API.  Since the
     * data models are large, higher levels of concurrency will have an impact on memory
     * pressure.
     */
    abstract val develocityMaxConcurrency: Property<Int>

    /**
     * The maximum amount of time, in seconds, to wait for an individual build scan to be processed.
     * If a build scan takes longer than this amount of time, it will be discarded / skipped,
     * mainly to prevent hanging the entire data collection process.  By default, the timeout
     * is set to 2 minutes.
     */
    abstract val buildScanRetrievalTimeout: Property<Int>

    /**
     * Custom build data summarizers to apply to the build data.  These capture the details
     * of the build that are important to report upon and summarize them in a way that can
     * be aggregated/reduced.
     */
    abstract val summarizers: ListProperty<MetricSummarizer<*>>
}