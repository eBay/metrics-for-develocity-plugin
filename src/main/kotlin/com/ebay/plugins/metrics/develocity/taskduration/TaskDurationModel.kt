package com.ebay.plugins.metrics.develocity.taskduration

import com.ebay.plugins.metrics.develocity.projectcost.merge
import kotlinx.serialization.Serializable

/**
 * Intermediate data model used to aggregate the total execution duration of tasks.
 * Stores data by both task name (across all modules) and by task type.  These details
 * are store both in an aggregate total as well as with a daily breakdown.
 */
@Serializable
data class TaskDurationSummary(
    // Map of task name to its execution data
    val taskNameData: Map<String, TaskExecutionSummary> = emptyMap(),
    // Map of fully qualified task type/class name to its execution data
    val taskTypeData: Map<String, TaskExecutionSummary> = emptyMap(),
) {
    operator fun plus(other: TaskDurationSummary): TaskDurationSummary {
        // Note: Because we're starting with a Map instead of a MutableMap,
        //       Map.merge creates a new map rather than actually merge into the current Maps
        return TaskDurationSummary(
            taskNameData = taskNameData.merge(other.taskNameData) { left, right ->
                left + right
            },
            taskTypeData = taskTypeData.merge(other.taskTypeData) { left, right ->
                left + right
            },
        )
    }
}

/**
 * Data model representing execution summary for a task, with a by-day breakdown.
 */
@Serializable
data class TaskExecutionSummary(
    val total: TaskExecutionData = TaskExecutionData(),
    val byDay: Map<String, TaskExecutionData> = emptyMap(),
) {
    operator fun plus(other: TaskExecutionSummary): TaskExecutionSummary {
        // Note: Because we're starting with a Map instead of a MutableMap,
        // Map.merge creates a new map rather than actually merge into the current Maps:
        return TaskExecutionSummary(
            total = total + other.total,
            byDay = byDay.merge(other.byDay) { left, right ->
                left + right
            }
        )
    }
}

/**
 * Data for a single task's execution metrics.
 */
@Serializable
data class TaskExecutionData(
    val totalDuration: Long = 0,
    val executionCount: Long = 0,
    val minDuration: Long = totalDuration,
    val maxDuration: Long = totalDuration,
) {
    operator fun plus(other: TaskExecutionData): TaskExecutionData {
        return TaskExecutionData(
            totalDuration = totalDuration + other.totalDuration,
            executionCount = executionCount + other.executionCount,
            minDuration = when {
                executionCount != 0L && other.executionCount != 0L -> minOf(minDuration, other.minDuration)
                executionCount != 0L -> minDuration
                else -> other.minDuration
            },
            maxDuration = when {
                executionCount != 0L && other.executionCount != 0L -> maxOf(maxDuration, other.maxDuration)
                executionCount != 0L -> maxDuration
                else -> other.maxDuration
            },
        )
    }
}
