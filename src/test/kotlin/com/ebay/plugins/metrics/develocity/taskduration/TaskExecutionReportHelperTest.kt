package com.ebay.plugins.metrics.develocity.taskduration

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.testng.annotations.Test

class TaskExecutionReportHelperTest {

    @Test
    fun generateDailyBreakdownTableWithEmptyData() {
        // Given
        val emptyData = emptyMap<String, TaskExecutionData>()

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(emptyData)

        // Then
        assertThat(result, equalTo("No daily breakdown data available."))
    }

    @Test
    fun generateDailyBreakdownTableWithSampleData() {
        // Given
        val dailyData = mapOf(
            "2024-01-15" to TaskExecutionData(
                totalDuration = 75000L, // 1 min 15 seconds
                executionCount = 3L,
                minDuration = 20000L, // 20 seconds
                maxDuration = 35000L  // 35 seconds
            ),
            "2024-01-14" to TaskExecutionData(
                totalDuration = 50000L, // 50 seconds
                executionCount = 2L,
                minDuration = 15000L, // 15 seconds
                maxDuration = 35000L  // 35 seconds
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |2024-01-14       2        50s        15s        25s        35s
            |2024-01-15       3      1m15s        20s        25s        35s
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }

    @Test
    fun generateDailyBreakdownTableSortsDataByDate() {
        // Given
        val dailyData = mapOf(
            "2024-01-13" to TaskExecutionData(
                totalDuration = 30000L, // 30 seconds total
                executionCount = 1L,    // avg = 30 seconds
                minDuration = 30000L,
                maxDuration = 30000L
            ),
            "2024-01-14" to TaskExecutionData(
                totalDuration = 80000L, // 80 seconds total
                executionCount = 2L,    // avg = 40 seconds
                minDuration = 35000L,
                maxDuration = 45000L
            ),
            "2024-01-15" to TaskExecutionData(
                totalDuration = 50000L, // 50 seconds total
                executionCount = 2L,    // avg = 25 seconds
                minDuration = 20000L,
                maxDuration = 30000L
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then - Should be sorted by date, ascending
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |2024-01-13       1        30s        30s        30s        30s
            |2024-01-14       2      1m20s        35s        40s        45s
            |2024-01-15       2        50s        20s        25s        30s
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }

    @Test
    fun generateDailyBreakdownTableShowsGlobalMinMaxDuration() {
        // Given
        val dailyData = mapOf(
            "2024-01-15" to TaskExecutionData(
                totalDuration = 60000L,
                executionCount = 2L,
                minDuration = 25000L, // 25 seconds
                maxDuration = 35000L  // 35 seconds
            ),
            "2024-01-14" to TaskExecutionData(
                totalDuration = 40000L,
                executionCount = 2L,
                minDuration = 15000L, // 15 seconds (global min)
                maxDuration = 25000L  // 25 seconds
            ),
            "2024-01-13" to TaskExecutionData(
                totalDuration = 90000L,
                executionCount = 2L,
                minDuration = 40000L, // 40 seconds
                maxDuration = 50000L  // 50 seconds (global max)
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then - Should show global min (15s) and max (50s) for all rows, sorted by avg duration descending
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |2024-01-13       2      1m30s        40s        45s        50s
            |2024-01-14       2        40s        15s        20s        25s
            |2024-01-15       2       1m0s        25s        30s        35s
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }

    @Test
    fun generateDailyBreakdownTableHandlesZeroExecutionCount() {
        // Given
        val dailyData = mapOf(
            "2024-01-15" to TaskExecutionData(
                totalDuration = 60000L,
                executionCount = 2L,
                minDuration = 25000L,
                maxDuration = 35000L
            ),
            "2024-01-14" to TaskExecutionData(
                totalDuration = 0L,
                executionCount = 0L, // Zero executions
                minDuration = Long.MAX_VALUE, // Default value for no executions
                maxDuration = 0L
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then - Should handle zero division gracefully for average and show proper global min/max
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |2024-01-14       0         0s         0s         0s         0s
            |2024-01-15       2       1m0s        25s        30s        35s
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }

    @Test
    fun generateDailyBreakdownTableFormattingScenarios() {
        // Given - Test all formatDuration scenarios: hours, minutes, seconds, seconds+ms, ms only, zero
        val dailyData = mapOf(
            "h" to TaskExecutionData(
                totalDuration = 7265432L, // 2h 1m 5s 432ms
                executionCount = 1L,
                minDuration = 7265432L,
                maxDuration = 7265432L
            ),
            "m" to TaskExecutionData(
                totalDuration = 125000L, // 2m 5s (no ms part)
                executionCount = 1L,
                minDuration = 125000L,
                maxDuration = 125000L
            ),
            "s-ms" to TaskExecutionData(
                totalDuration = 5432L, // 5s 432ms
                executionCount = 1L,
                minDuration = 5432L,
                maxDuration = 5432L
            ),
            "s-only" to TaskExecutionData(
                totalDuration = 15000L, // 15s (no ms part)
                executionCount = 1L,
                minDuration = 15000L,
                maxDuration = 15000L
            ),
            "ms" to TaskExecutionData(
                totalDuration = 432L, // 432ms (less than 1 second)
                executionCount = 1L,
                minDuration = 432L,
                maxDuration = 432L
            ),
            "else" to TaskExecutionData(
                totalDuration = 0L, // 0s
                executionCount = 0L,
                minDuration = Long.MAX_VALUE, // No executions
                maxDuration = 0L
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then - Verify all formatting scenarios (sorted by avg duration descending)
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |else             0         0s         0s         0s         0s
            |h                1     2h1m5s     2h1m5s     2h1m5s     2h1m5s
            |m                1       2m5s       2m5s       2m5s       2m5s
            |ms               1      432ms      432ms      432ms      432ms
            |s-ms             1    5s432ms    5s432ms    5s432ms    5s432ms
            |s-only           1        15s        15s        15s        15s
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }

    @Test
    fun generateDailyBreakdownTableMinMaxEdgeCases() {
        // Given - Test edge cases for min/max duration calculations
        val dailyData = mapOf(
            "normal" to TaskExecutionData(
                totalDuration = 60000L, // 1m
                executionCount = 2L,
                minDuration = 25000L, // 25s
                maxDuration = 35000L  // 35s
            ),
            "small-min" to TaskExecutionData(
                totalDuration = 40000L, // 40s
                executionCount = 2L,
                minDuration = 1L, // 1ms (global minimum)
                maxDuration = 20000L // 20s
            ),
            "large-max" to TaskExecutionData(
                totalDuration = 180000L, // 3m
                executionCount = 1L,
                minDuration = 180000L, // 3m
                maxDuration = 14400000L // 4h (global maximum)
            ),
            "low-exec" to TaskExecutionData(
                totalDuration = 10000L, // 10s
                executionCount = 1L,
                minDuration = 10000L, // 10s
                maxDuration = 10000L // 10s
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then - Should show global min (1ms) and max (4h) for all rows, sorted by avg duration descending
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |large-max        1       3m0s       3m0s       3m0s     4h0m0s
            |low-exec         1        10s        10s        10s        10s
            |normal           2       1m0s        25s        30s        35s
            |small-min        2        40s        1ms        20s        20s
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }

    @Test
    fun generateDailyBreakdownTableAllDataHasMaxLongValue() {
        // Given - Edge case where all data has Long.MAX_VALUE for minDuration (no executions anywhere)
        val dailyData = mapOf(
            "empty1" to TaskExecutionData(
                totalDuration = Long.MAX_VALUE,
                executionCount = 0L,
                minDuration = Long.MAX_VALUE,
                maxDuration = Long.MAX_VALUE,
            ),
            "empty2" to TaskExecutionData(
                totalDuration = Long.MAX_VALUE,
                executionCount = 0L,
                minDuration = Long.MAX_VALUE,
                maxDuration = Long.MAX_VALUE,
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then - Should handle Long.MAX_VALUE gracefully (shows 0s for min when no executions)
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |empty1           0         0s         0s         0s         0s
            |empty2           0         0s         0s         0s         0s
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }

    @Test
    fun generateDailyBreakdownTableSingleDataPoint() {
        // Given - Only one data point to test min/max edge case
        val dailyData = mapOf(
            "single" to TaskExecutionData(
                totalDuration = 42500L, // 42s 500ms
                executionCount = 1L,
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then - Min, avg, and max should all be the same value
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |single           1   42s500ms   42s500ms   42s500ms   42s500ms
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }

    @Test
    fun generateDailyBreakdownTableMultiDayDurationFormatting() {
        // Given - Test formatting of durations that span multiple days
        val multiDayDurationMs = (2 * 24 * 60 * 60 * 1000L) + // 2 days
                                (5 * 60 * 60 * 1000L) +        // 5 hours
                                (30 * 60 * 1000L) +            // 30 minutes
                                (45 * 1000L) +                 // 45 seconds
                                123L                            // 123 milliseconds
        // Total: 2d5h30m45s123ms = 191,445,123 milliseconds

        val dailyData = mapOf(
            "multi-day" to TaskExecutionData(
                totalDuration = multiDayDurationMs,
                executionCount = 1L,
                minDuration = multiDayDurationMs,
                maxDuration = multiDayDurationMs
            )
        )

        // When
        val result = TaskExecutionReportHelper.generateDailyBreakdownTable(dailyData)

        // Then - Should format multi-day duration correctly
        val expected = """
            |Daily Breakdown
            |===============
            |
            |Date         Count      Total        Min        Avg        Max
            |---------- ------- ---------- ---------- ---------- ----------
            |multi-day        1    2d5h30m    2d5h30m    2d5h30m    2d5h30m
        """.trimMargin()

        assertThat(result, equalTo(expected))
    }
}