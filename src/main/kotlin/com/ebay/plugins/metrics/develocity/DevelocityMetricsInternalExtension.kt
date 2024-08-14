package com.ebay.plugins.metrics.develocity

import org.gradle.api.tasks.TaskProvider

/**
 * Gradle extension used to expose plugin internals to the task providers used by the
 * end user.
 */
internal open class DevelocityMetricsInternalExtension(
    private val hourlyTaskProvider: (timeSpec: String) -> TaskProvider<GatherHourlyTask>,
    private val dailyTaskProvider: (timeSpec: String) -> TaskProvider<GatherAggregateTask>,
    private val durationTaskProvider: (durationSpec: String) -> TaskProvider<GatherAggregateTask>,
) {
    /**
     * Create a task provider for gathering hourly metric data, given a time specification.
     */
    fun createHourlyTask(timeSpec: String): TaskProvider<GatherHourlyTask> = hourlyTaskProvider(timeSpec)

    /**
     * Create a task provider for gathering daily metric data, given a time specification.
     */
    fun createDailyTask(timeSpec: String): TaskProvider<GatherAggregateTask> = dailyTaskProvider(timeSpec)

    /**
     * Create a task provider for gathering metric data over a duration, given a duration specification.
     */
    fun createDurationTask(durationSpec: String): TaskProvider<GatherAggregateTask> = durationTaskProvider(durationSpec)
}