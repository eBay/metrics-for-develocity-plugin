package com.ebay.plugins.metrics.develocity

import org.gradle.api.provider.ValueSource
import java.time.Instant

/**
 * [ValueSource] implementation which determines what the current day and hour is.
 */
abstract class CurrentDayWithHourValueSource : ValueSource<String, CurrentDayWithHourParameters> {
    override fun obtain(): String {
        val dateHelper = DateHelper(parameters.zoneId)
        return dateHelper.toHourlyString(Instant.now())
    }
}