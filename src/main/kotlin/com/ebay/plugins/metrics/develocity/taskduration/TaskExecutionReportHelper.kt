package com.ebay.plugins.metrics.develocity.taskduration

import com.ebay.plugins.metrics.develocity.DateHelper
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

/**
 * Common helper for generating task execution reports.
 */
object TaskExecutionReportHelper {
    fun writeTaskExecutionReport(
        logger: Logger,
        reportTitle: String,
        executionSummary: TaskExecutionSummary?,
        reportFile: RegularFileProperty,
        timeZoneIdProvider: Provider<String>,
    ) {
        val dateHelper = DateHelper(timeZoneIdProvider)
        val now = Instant.now()
        val execSummary = executionSummary ?: TaskExecutionSummary() // Default to empty data if not found
        val execData = execSummary.total
        val totalDurationMillis = execData.totalDuration
        val totalDurationFormatted = formatDuration(totalDurationMillis)
        val avgDurationMillis = if (execData.executionCount > 0)
            totalDurationMillis / execData.executionCount else 0
        val avgDurationFormatted = formatDuration(avgDurationMillis)

        val dailyBreakdown = generateDailyBreakdownTable(execSummary.byDay)

        val report = """
            |$reportTitle
            |${"=".repeat(reportTitle.length)}
            |
            |Report Generated At: ${dateHelper.toHourlyString(now)} (${timeZoneIdProvider.get()})
            |Total Execution Duration: $totalDurationFormatted
            |Number of Task Executions: ${execData.executionCount}
            |Average Execution Duration: $avgDurationFormatted
            |
            |$dailyBreakdown
        """.trimMargin().replace("\n", System.lineSeparator())

        reportFile.asFile.get().also { reportFile ->
            logger.lifecycle("Task duration report available at: file://${reportFile.absolutePath}")
            reportFile.writeText(report)
        }
    }

    internal fun generateDailyBreakdownTable(byDay: Map<String, TaskExecutionData>): String {
        if (byDay.isEmpty()) {
            return "No daily breakdown data available."
        }

        val sortedDays = byDay.entries.sortedBy { (day, _) -> day }
        val header = "Daily Breakdown"
        val separator = "=".repeat(header.length)
        val dateWidth = 10
        val durationWidth = 10
        val countWidth = 7

        val headerRow = String.format(
            "%-${dateWidth}s %${countWidth}s %${durationWidth}s %${durationWidth}s %${durationWidth}s %${durationWidth}s",
            "Date", "Count", "Total", "Min", "Avg", "Max"
        )
        val headerSeparator = buildString {
            append("-".repeat(dateWidth))
            append(" ").append("-".repeat(countWidth))
            for (x in 1..4) {
                append(" ").append("-".repeat(durationWidth))
            }
        }

        val rows = sortedDays.map { (date, data) ->
            val minDuration: Long
            val maxDuration: Long
            val avgDuration: Long
            val totalDuration: Long
            if (data.executionCount > 0) {
                minDuration = data.minDuration
                maxDuration = data.maxDuration
                avgDuration = data.totalDuration / data.executionCount
                totalDuration = data.totalDuration
            } else {
                minDuration = 0L
                maxDuration = 0L
                avgDuration = 0L
                totalDuration = 0L
            }

            String.format(
                "%-${dateWidth}s %${countWidth}d %${durationWidth}s %${durationWidth}s %${durationWidth}s %${durationWidth}s",
                date,
                data.executionCount,
                formatDuration(totalDuration),
                formatDuration(minDuration),
                formatDuration(avgDuration),
                formatDuration(maxDuration),
            )
        }

        return listOf(header, separator, "", headerRow, headerSeparator)
            .plus(rows)
            .joinToString(System.lineSeparator())
    }

    private fun formatDuration(millis: Long): String {
        val duration = millis.milliseconds
        val days = duration.inWholeDays
        val hours = duration.inWholeHours % 24
        val minutes = duration.inWholeMinutes % 60
        val seconds = duration.inWholeSeconds % 60
        val millisPart = duration.inWholeMilliseconds % 1000

        return when {
            days > 0 && days < Long.MAX_VALUE-> "${days}d${hours}h${minutes}m"
            hours > 0 -> "${hours}h${minutes}m${seconds}s"
            minutes > 0 -> "${minutes}m${seconds}s"
            seconds > 0 && millisPart > 0 -> "${seconds}s${millisPart}ms"
            seconds > 0 -> "${seconds}s"
            millisPart > 0 -> "${millisPart}ms"
            else -> "0s"
        }
    }
}
