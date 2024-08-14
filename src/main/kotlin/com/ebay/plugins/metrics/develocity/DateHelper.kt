package com.ebay.plugins.metrics.develocity

import org.gradle.api.provider.Provider
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Utility class for working with datetime values, pre-configured with a reference timezone.
 */
class DateHelper(
    private val zoneIdProvider: Provider<String>
) {
    private val zoneId by lazy {
        ZoneId.of(zoneIdProvider.get())
    }

    fun toHourlyString(instant: Instant): String {
        return DATETIME_HOURLY_FORMATTER
            .withZone(zoneId)
            .format(instant)
    }

    fun fromHourlyString(value: String): Instant {
        val result = DATETIME_HOURLY_FORMATTER
            .withZone(zoneId)
            .parse(value)
        return Instant.from(result)
    }

    fun toDailyString(instant: Instant): String {
        return DATETIME_DAILY_FORMATTER
            .withZone(zoneId)
            .format(instant)
    }

    fun fromDailyString(value: String): Instant {
        return LocalDate.parse(value).atStartOfDay(zoneId).toInstant()
    }

    companion object {
        private val DATETIME_HOURLY_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH")
        private val DATETIME_DAILY_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
    }
}