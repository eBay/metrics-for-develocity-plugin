package com.ebay.plugins.metrics.develocity

/**
 * Utility functions for generating task names, to keep it all in one place.
 */
internal object NameUtil {
    private const val TASK_PREFIX = "metricsForDevelocity"

    /**
     * Pattern to match the date portion of a datetime string.
     */
    private const val DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}"

    /**
     * Pattern to match the hour portion of a datetime string.
     */
    private const val HOUR_PATTERN = "\\d{2}"

    /**
     * Pattern to match the subset of the `Duration` string format defined by Java:
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html#parse(java.lang.CharSequence)
     *
     * We only support days and hours, and we don't support minutes or seconds.
     * We also only support positive duration values.
     */
    private const val DURATION_PATTERN =
        "[pP](?:[0-9]+[dD])?(?:T(?:[0-9]+[hH]?)?)?"

    /**
     * Regular expression used to match requested task names that have a name suffixed by a datetime specification.
     *
     * Examples:
     * - metricsForDevelocity-2024-10-18
     * - speedReport-2024-10-18T09
     */
    internal val DATETIME_SUFFIX_PATTERN = Regex(
        ".*-($DATE_PATTERN(?:T${HOUR_PATTERN})?)$"
    )

    /**
     * Regular expression used to match requested task names that have a name suffixed by a duration specification.
     *
     * Examples:
     * - metricsForDevelocity-last-P7D
     * - speedReport-P2DT12H
     * - bobsReport-PT8H
     */
    internal val DURATION_SUFFIX_PATTERN = Regex(
        ".*-($DURATION_PATTERN)$"
    )

    val DATETIME_TASK_PATTERN = Regex(
        // Examples:
        //      metricsForDevelocity-2024-06-01
        //      metricsForDevelocity-2024-06-01T05
        "^\\Q$TASK_PREFIX-\\E(($DATE_PATTERN)(?:T(${HOUR_PATTERN}))?)$"
    ).toPattern()

    val DURATION_TASK_PATTERN = Regex(
        // Examples:
        //      metricsForDevelocity-last-P7D
        //      metricsForDevelocity-last-PT8H
        //      metricsForDevelocity-last-P2DT8H
        "^\\Q$TASK_PREFIX-last-\\E($DURATION_PATTERN)$"
    ).toPattern()

    fun dateTime(timeSpec: String): String = "$TASK_PREFIX-$timeSpec"

    fun duration(durationStr: String): String = "$TASK_PREFIX-last-$durationStr"
}
