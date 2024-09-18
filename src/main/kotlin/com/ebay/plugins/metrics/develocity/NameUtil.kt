package com.ebay.plugins.metrics.develocity

/**
 * Utility functions for generating task names, to keep it all in one place.
 */
internal object NameUtil {
    const val TASK_PREFIX = "metricsForDevelocity"

    val DATETIME_TASK_PATTERN = Regex(
        // Examples:
        //      metricsForDevelocity-2024-06-01
        //      metricsForDevelocity-2024-06-01T05
        "^\\Q$TASK_PREFIX-\\E((\\d{4}-\\d{2}-\\d{2})(?:T(\\d{2}))?)$"
    ).toPattern()

    val DURATION_TASK_PATTERN = Regex(
        // Examples:
        //      metricsForDevelocity-last-P7D
        //      metricsForDevelocity-last-PT8H
        //      metricsForDevelocity-last-P2DT8H
        "^\\Q$TASK_PREFIX-last-\\E(\\w+)$"
    ).toPattern()

    fun dateTime(timeSpec: String): String = "$TASK_PREFIX-$timeSpec"

    fun duration(durationStr: String): String = "$TASK_PREFIX-last-$durationStr"
}
