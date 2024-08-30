package com.ebay.plugins.metrics.develocity.projectcost

import kotlinx.serialization.Serializable

/**
 * Intermediate data model for calculating the build cost on a per-project basis.
 */
@Serializable
data class ProjectCostSummary(
    val projectData: Map<String, ProjectData> = emptyMap(),
    val totalBuildCount: Long = 0L,
    val totalBuildTime: Long = 0L,
)

/**
 * Intermediate data model for the build costs associated with a single project module.
 */
@Serializable
data class ProjectData(
    /**
     * The total amount of time (in milliseconds) spent running tasks in the project module.
     */
    val buildDuration: Long = 0L,

    /**
     * The list of build IDs for builds which executed tasks in this project.
     */
    val buildsWithExecution: Set<String> = emptySet(),

    /**
     * The list of user names who have been impacted by the build costs of the project module.
     */
    val impactedUsers: Set<String> = emptySet(),

    /**
     * The total number of builds in which the project module was included in the build.
     */
    val includedBuildCount: Long = 0L,

    /**
     * Map of task name to per-task build cost data.
     */
    val taskNameToData: Map<String, TaskData> = emptyMap(),

    /**
     * Map of task type to per-task build cost data.
     */
    val taskTypeToData: Map<String, TaskData> = emptyMap(),
) {
    fun plus(other: ProjectData): ProjectData {
        return ProjectData(
            buildsWithExecution = buildsWithExecution + other.buildsWithExecution,
            buildDuration = buildDuration + other.buildDuration,
            impactedUsers = impactedUsers + other.impactedUsers,
            includedBuildCount = includedBuildCount + other.includedBuildCount,
            taskNameToData = taskNameToData.merge(other.taskNameToData) { a, b -> a.plus(b) },
            taskTypeToData = taskTypeToData.merge(other.taskTypeToData) { a, b -> a.plus(b) },
        )
    }
}

/**
 * Intermediate data model for the build costs associated with a single task.
 */
@Serializable
data class TaskData(
    val count: Long = 0L,
    val executed: Long = 0L,
    val duration: Long = 0L,
) {
    fun plus(other: TaskData): TaskData {
        return TaskData(
            count = count + other.count,
            executed = executed + other.executed,
            duration = duration + other.duration,
        )
    }
}

/**
 * Final report model.
 */
@Serializable
data class ProjectCostReport(
    val projectData: Map<String, ProjectModuleCostReport> = emptyMap(),
    val totalBuildCount: Long = 0L,
    val totalBuildTime: Long = 0L,
)

/**
 * Final report model for each project module.
 */
@Serializable
data class ProjectModuleCostReport(
    /**
     * Total duration of all executed tasks within the project module divided by the number of builds in
     * which at least one task was executed.
     */
    val buildAvgDuration: Long,

    /**
     * The average number of tasks executed within the project module per build in which at least one task
     * was executed.
     */
    val buildAvgTasks: Long,

    /**
     * A generate composite value which represents the cost of the project module, meant to be used to
     * perform relative comparisons between project modules.  While the algorithm for calculating this
     * may change over time, it is currently calculated as the [buildAvgDuration] multiplied by
     * the [buildPercentage], truncated to an integer value.  This is meant to surface modules
     * which are built frequently that are also expensive to build.
     */
    val buildCostScalar: Long,

    /**
     * The number of builds in which at least one task was executed within the project module.  A task is
     * considered executed if it did work (e.g. was not up-to-date, pulled from cache, etc.).
     */
    val buildCount: Int,

    /**
     * The total duration of all tasks within the project module.  This includes the costs of tasks which
     * were up-to-date, pulled from cache, etc.
     */
    val buildDuration: Long,

    /**
     * The percentage of builds in which at least one task was executed within the project module.
     */
    val buildPercentage: Float,

    /**
     * The total number of unique user IDs who have been impacted by the build costs of the project module.
     */
    val impactedUserCount: Int,
)