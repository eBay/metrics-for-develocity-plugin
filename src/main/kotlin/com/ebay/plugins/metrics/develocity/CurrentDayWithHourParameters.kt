package com.ebay.plugins.metrics.develocity

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSourceParameters] for the [CurrentDayWithHourParameters] value source.
 */
abstract class CurrentDayWithHourParameters : ValueSourceParameters {
    /**
     * Timezone ID to use when determining the boundaries of the "current day".
     */
    abstract val zoneId: Property<String>
}