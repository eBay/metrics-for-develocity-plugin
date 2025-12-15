package com.ebay.plugins.metrics.develocity.taskduration

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.not
import org.testng.annotations.Test

class TaskDurationModelTest {

    // Tests for TaskExecutionData.plus() function
    @Test
    fun taskExecutionDataPlusWithBothHavingData() {
        // Given
        val first = TaskExecutionData(
            totalDuration = 1000L,
            executionCount = 2L,
            minDuration = 300L,
            maxDuration = 700L
        )
        val second = TaskExecutionData(
            totalDuration = 1500L,
            executionCount = 3L,
            minDuration = 200L,
            maxDuration = 800L
        )

        // When
        val result = first + second

        // Then
        assertThat(result.totalDuration, equalTo(2500L))
        assertThat(result.executionCount, equalTo(5L))
        assertThat(result.minDuration, equalTo(200L)) // min of 300L and 200L
        assertThat(result.maxDuration, equalTo(800L)) // max of 700L and 800L
    }

    @Test
    fun taskExecutionDataPlusWithFirstEmpty() {
        // Given
        val empty = TaskExecutionData()
        val withData = TaskExecutionData(
            totalDuration = 1500L,
            executionCount = 3L,
            minDuration = 200L,
            maxDuration = 800L
        )

        // When
        val result = empty + withData

        // Then
        assertThat(result.totalDuration, equalTo(1500L))
        assertThat(result.executionCount, equalTo(3L))
        assertThat(result.minDuration, equalTo(200L)) // Should take the second's minDuration
        assertThat(result.maxDuration, equalTo(800L)) // Should take the second's maxDuration
    }

    @Test
    fun taskExecutionDataPlusWithSecondEmpty() {
        // Given
        val withData = TaskExecutionData(
            totalDuration = 1500L,
            executionCount = 3L,
            minDuration = 200L,
            maxDuration = 800L
        )
        val empty = TaskExecutionData()

        // When
        val result = withData + empty

        // Then
        assertThat(result.totalDuration, equalTo(1500L))
        assertThat(result.executionCount, equalTo(3L))
        assertThat(result.minDuration, equalTo(200L)) // Should keep the first's minDuration
        assertThat(result.maxDuration, equalTo(800L)) // Should keep the first's maxDuration
    }

    @Test
    fun taskExecutionDataPlusWithBothEmpty() {
        // Given
        val empty1 = TaskExecutionData()
        val empty2 = TaskExecutionData()

        // When
        val result = empty1 + empty2

        // Then
        assertThat(result.totalDuration, equalTo(0L))
        assertThat(result.executionCount, equalTo(0L))
        assertThat(result.minDuration, equalTo(0)) // Takes other.minDuration when first executionCount == 0L
        assertThat(result.maxDuration, equalTo(0L)) // Takes other.maxDuration when first executionCount == 0L
    }

    @Test
    fun taskExecutionDataPlusWithOverlappingBuildIds() {
        // Given
        val first = TaskExecutionData(
            totalDuration = 1000L,
            executionCount = 2L,
            minDuration = 300L,
            maxDuration = 700L
        )
        val second = TaskExecutionData(
            totalDuration = 1500L,
            executionCount = 3L,
            minDuration = 200L,
            maxDuration = 800L
        )

        // When
        val result = first + second

        // Then
        assertThat(result.totalDuration, equalTo(2500L))
        assertThat(result.executionCount, equalTo(5L))
        assertThat(result.minDuration, equalTo(200L))
        assertThat(result.maxDuration, equalTo(800L))
    }

    @Test
    fun taskExecutionDataPlusWithSameMinMaxValues() {
        // Given
        val first = TaskExecutionData(
            totalDuration = 1000L,
            executionCount = 2L,
            minDuration = 500L,
            maxDuration = 500L
        )
        val second = TaskExecutionData(
            totalDuration = 1500L,
            executionCount = 3L,
            minDuration = 500L,
            maxDuration = 500L
        )

        // When
        val result = first + second

        // Then
        assertThat(result.totalDuration, equalTo(2500L))
        assertThat(result.executionCount, equalTo(5L))
        assertThat(result.minDuration, equalTo(500L))
        assertThat(result.maxDuration, equalTo(500L))
    }

    // Tests for TaskExecutionSummary.merge() function
    @Test
    fun taskExecutionSummaryMergeWithBothHavingData() {
        // Given
        val first = TaskExecutionSummary(
            total = TaskExecutionData(
                totalDuration = 1000L,
                executionCount = 2L,
                minDuration = 300L,
                maxDuration = 700L
            ),
            byDay = mutableMapOf(
                "2024-01-15" to TaskExecutionData(
                    totalDuration = 500L,
                    executionCount = 1L,
                    minDuration = 500L,
                    maxDuration = 500L
                ),
                "2024-01-14" to TaskExecutionData(
                    totalDuration = 500L,
                    executionCount = 1L,
                    minDuration = 500L,
                    maxDuration = 500L
                )
            )
        )
        val second = TaskExecutionSummary(
            total = TaskExecutionData(
                totalDuration = 1500L,
                executionCount = 3L,
                minDuration = 200L,
                maxDuration = 800L
            ),
            byDay = mutableMapOf(
                "2024-01-15" to TaskExecutionData(
                    totalDuration = 600L,
                    executionCount = 1L,
                    minDuration = 600L,
                    maxDuration = 600L
                ),
                "2024-01-13" to TaskExecutionData(
                    totalDuration = 900L,
                    executionCount = 2L,
                    minDuration = 400L,
                    maxDuration = 500L
                )
            )
        )

        // When
        val result = first + second

        // Then
        assertThat(result.total.totalDuration, equalTo(2500L))
        assertThat(result.total.executionCount, equalTo(5L))
        assertThat(result.total.minDuration, equalTo(200L))
        assertThat(result.total.maxDuration, equalTo(800L))

        // Check byDay merging
        assertThat(result.byDay.size, equalTo(3))
        assertThat(result.byDay, hasEntry("2024-01-15", TaskExecutionData(
            totalDuration = 1100L,
            executionCount = 2L,
            minDuration = 500L,
            maxDuration = 600L
        )))
        assertThat(result.byDay, hasEntry("2024-01-14", TaskExecutionData(
            totalDuration = 500L,
            executionCount = 1L,
            minDuration = 500L,
            maxDuration = 500L
        )))
        assertThat(result.byDay, hasEntry("2024-01-13", TaskExecutionData(
            totalDuration = 900L,
            executionCount = 2L,
            minDuration = 400L,
            maxDuration = 500L
        )))
    }

    @Test
    fun taskExecutionSummaryMergeWithEmptyFirst() {
        // Given
        val empty = TaskExecutionSummary()
        val withData = TaskExecutionSummary(
            total = TaskExecutionData(
                totalDuration = 1500L,
                executionCount = 3L,
                minDuration = 200L,
                maxDuration = 800L
            ),
            byDay = mutableMapOf(
                "2024-01-15" to TaskExecutionData(
                    totalDuration = 600L,
                    executionCount = 1L,
                    minDuration = 600L,
                    maxDuration = 600L
                )
            )
        )

        // When
        val result = empty + withData

        // Then
        assertThat(result.total.totalDuration, equalTo(1500L))
        assertThat(result.total.executionCount, equalTo(3L))
        assertThat(result.total.minDuration, equalTo(200L))
        assertThat(result.total.maxDuration, equalTo(800L))
        assertThat(result.byDay.size, equalTo(1))
        assertThat(result.byDay, hasEntry("2024-01-15", TaskExecutionData(
            totalDuration = 600L,
            executionCount = 1L,
            minDuration = 600L,
            maxDuration = 600L
        )))
    }

    @Test
    fun taskExecutionSummaryMergeWithEmptySecond() {
        // Given
        val withData = TaskExecutionSummary(
            total = TaskExecutionData(
                totalDuration = 1500L,
                executionCount = 3L,
                minDuration = 200L,
                maxDuration = 800L
            ),
            byDay = mutableMapOf(
                "2024-01-15" to TaskExecutionData(
                    totalDuration = 600L,
                    executionCount = 1L,
                    minDuration = 600L,
                    maxDuration = 600L
                )
            )
        )
        val empty = TaskExecutionSummary()

        // When
        val result = withData + empty

        // Then
        assertThat(result.total.totalDuration, equalTo(1500L))
        assertThat(result.total.executionCount, equalTo(3L))
        assertThat(result.total.minDuration, equalTo(200L))
        assertThat(result.total.maxDuration, equalTo(800L))
        assertThat(result.byDay.size, equalTo(1))
        assertThat(result.byDay, hasEntry("2024-01-15", TaskExecutionData(
            totalDuration = 600L,
            executionCount = 1L,
            minDuration = 600L,
            maxDuration = 600L
        )))
    }

    @Test
    fun taskExecutionSummaryMergeWithBothEmpty() {
        // Given
        val empty1 = TaskExecutionSummary()
        val empty2 = TaskExecutionSummary()

        // When
        val result = empty1 + empty2

        // Then
        assertThat(result.total.totalDuration, equalTo(0L))
        assertThat(result.total.executionCount, equalTo(0L))
        assertThat(result.total.minDuration, equalTo(0L))
        assertThat(result.total.maxDuration, equalTo(0L))
        assertThat(result.byDay.size, equalTo(0))
    }

    @Test
    fun taskExecutionSummaryMergeCreatesNewByDayMap() {
        // Given
        val originalByDay = mutableMapOf("2024-01-15" to TaskExecutionData(
            totalDuration = 500L,
            executionCount = 1L,
            minDuration = 500L,
            maxDuration = 500L
        ))
        val first = TaskExecutionSummary(
            total = TaskExecutionData(
                totalDuration = 500L,
                executionCount = 1L,
                minDuration = 500L,
                maxDuration = 500L
            ),
            byDay = originalByDay
        )
        val second = TaskExecutionSummary(
            total = TaskExecutionData(
                totalDuration = 600L,
                executionCount = 1L,
                minDuration = 600L,
                maxDuration = 600L
            ),
            byDay = mutableMapOf("2024-01-15" to TaskExecutionData(
                totalDuration = 600L,
                executionCount = 1L,
                minDuration = 600L,
                maxDuration = 600L
            ))
        )

        // When
        val result = first + second

        // Then - The original byDay map should NOT be modified (we create a new map)
        assertThat(result.byDay, not(equalTo(originalByDay))) // They should be different references
        assertThat(result.byDay, hasEntry("2024-01-15", TaskExecutionData(
            totalDuration = 1100L,
            executionCount = 2L,
            minDuration = 500L,
            maxDuration = 600L
        )))
        // Original map should be unchanged
        assertThat(originalByDay, hasEntry("2024-01-15", TaskExecutionData(
            totalDuration = 500L,
            executionCount = 1L,
            minDuration = 500L,
            maxDuration = 500L
        )))
    }

    // Tests for TaskDurationSummary.plus() function
    @Test
    fun taskDurationSummaryPlusWithBothHavingData() {
        // Given
        val first = TaskDurationSummary(
            taskNameData = mapOf(
                "compile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1000L,
                        executionCount = 2L,
                        minDuration = 400L,
                        maxDuration = 600L
                    ),
                    byDay = mutableMapOf("2024-01-15" to TaskExecutionData(
                        totalDuration = 500L,
                        executionCount = 1L,
                        minDuration = 500L,
                        maxDuration = 500L
                    ))
                )
            ),
            taskTypeData = mapOf(
                "org.gradle.api.tasks.compile.JavaCompile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1200L,
                        executionCount = 3L,
                        minDuration = 300L,
                        maxDuration = 500L
                    ),
                    byDay = mutableMapOf("2024-01-15" to TaskExecutionData(
                        totalDuration = 600L,
                        executionCount = 1L,
                        minDuration = 600L,
                        maxDuration = 600L
                    ))
                )
            )
        )
        val second = TaskDurationSummary(
            taskNameData = mapOf(
                "compile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1500L,
                        executionCount = 3L,
                        minDuration = 200L,
                        maxDuration = 700L
                    ),
                    byDay = mutableMapOf("2024-01-14" to TaskExecutionData(
                        totalDuration = 750L,
                        executionCount = 1L,
                        minDuration = 750L,
                        maxDuration = 750L
                    ))
                ),
                "test" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 2000L,
                        executionCount = 2L,
                        minDuration = 900L,
                        maxDuration = 1100L
                    ),
                    byDay = mutableMapOf("2024-01-14" to TaskExecutionData(
                        totalDuration = 1000L,
                        executionCount = 1L,
                        minDuration = 1000L,
                        maxDuration = 1000L
                    ))
                )
            ),
            taskTypeData = mapOf(
                "org.gradle.api.tasks.compile.JavaCompile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1800L,
                        executionCount = 2L,
                        minDuration = 800L,
                        maxDuration = 1000L
                    ),
                    byDay = mutableMapOf("2024-01-14" to TaskExecutionData(
                        totalDuration = 900L,
                        executionCount = 1L,
                        minDuration = 900L,
                        maxDuration = 900L
                    ))
                ),
                "org.gradle.api.tasks.testing.Test" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 2500L,
                        executionCount = 1L,
                        minDuration = 2500L,
                        maxDuration = 2500L
                    ),
                    byDay = mutableMapOf("2024-01-14" to TaskExecutionData(
                        totalDuration = 2500L,
                        executionCount = 1L,
                        minDuration = 2500L,
                        maxDuration = 2500L
                    ))
                )
            )
        )

        // When
        val result = first + second

        // Then
        // Check taskNameData merging
        assertThat(result.taskNameData.size, equalTo(2))
        assertThat(result.taskNameData["compile"]?.total?.totalDuration, equalTo(2500L))
        assertThat(result.taskNameData["compile"]?.total?.executionCount, equalTo(5L))
        assertThat(result.taskNameData["compile"]?.total?.minDuration, equalTo(200L))
        assertThat(result.taskNameData["compile"]?.total?.maxDuration, equalTo(700L))
        assertThat(result.taskNameData["test"]?.total?.totalDuration, equalTo(2000L))

        // Check taskTypeData merging
        assertThat(result.taskTypeData.size, equalTo(2))
        assertThat(result.taskTypeData["org.gradle.api.tasks.compile.JavaCompile"]?.total?.totalDuration, equalTo(3000L))
        assertThat(result.taskTypeData["org.gradle.api.tasks.testing.Test"]?.total?.totalDuration, equalTo(2500L))
    }

    @Test
    fun taskDurationSummaryPlusWithEmptyFirst() {
        // Given
        val empty = TaskDurationSummary()
        val withData = TaskDurationSummary(
            taskNameData = mapOf(
                "compile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1000L,
                        executionCount = 2L,
                        minDuration = 400L,
                        maxDuration = 600L
                    ),
                    byDay = mutableMapOf("2024-01-15" to TaskExecutionData(
                        totalDuration = 500L,
                        executionCount = 1L,
                        minDuration = 500L,
                        maxDuration = 500L
                    ))
                )
            ),
            taskTypeData = mapOf(
                "org.gradle.api.tasks.compile.JavaCompile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1200L,
                        executionCount = 3L,
                        minDuration = 300L,
                        maxDuration = 500L
                    ),
                    byDay = mutableMapOf("2024-01-15" to TaskExecutionData(
                        totalDuration = 600L,
                        executionCount = 1L,
                        minDuration = 600L,
                        maxDuration = 600L
                    ))
                )
            )
        )

        // When
        val result = empty + withData

        // Then
        assertThat(result.taskNameData.size, equalTo(1))
        assertThat(result.taskNameData["compile"]?.total?.totalDuration, equalTo(1000L))
        assertThat(result.taskTypeData.size, equalTo(1))
        assertThat(result.taskTypeData["org.gradle.api.tasks.compile.JavaCompile"]?.total?.totalDuration, equalTo(1200L))
    }

    @Test
    fun taskDurationSummaryPlusWithEmptySecond() {
        // Given
        val withData = TaskDurationSummary(
            taskNameData = mapOf(
                "compile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1000L,
                        executionCount = 2L,
                        minDuration = 400L,
                        maxDuration = 600L
                    ),
                    byDay = mutableMapOf("2024-01-15" to TaskExecutionData(
                        totalDuration = 500L,
                        executionCount = 1L,
                        minDuration = 500L,
                        maxDuration = 500L
                    ))
                )
            ),
            taskTypeData = mapOf(
                "org.gradle.api.tasks.compile.JavaCompile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1200L,
                        executionCount = 3L,
                        minDuration = 300L,
                        maxDuration = 500L
                    ),
                    byDay = mutableMapOf("2024-01-15" to TaskExecutionData(
                        totalDuration = 600L,
                        executionCount = 1L,
                        minDuration = 600L,
                        maxDuration = 600L
                    ))
                )
            )
        )
        val empty = TaskDurationSummary()

        // When
        val result = withData + empty

        // Then
        assertThat(result.taskNameData.size, equalTo(1))
        assertThat(result.taskNameData["compile"]?.total?.totalDuration, equalTo(1000L))
        assertThat(result.taskTypeData.size, equalTo(1))
        assertThat(result.taskTypeData["org.gradle.api.tasks.compile.JavaCompile"]?.total?.totalDuration, equalTo(1200L))
    }

    @Test
    fun taskDurationSummaryPlusWithBothEmpty() {
        // Given
        val empty1 = TaskDurationSummary()
        val empty2 = TaskDurationSummary()

        // When
        val result = empty1 + empty2

        // Then
        assertThat(result.taskNameData.size, equalTo(0))
        assertThat(result.taskTypeData.size, equalTo(0))
    }

    @Test
    fun taskDurationSummaryPlusWithDisjointData() {
        // Given
        val first = TaskDurationSummary(
            taskNameData = mapOf(
                "compile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1000L,
                        executionCount = 2L,
                        minDuration = 400L,
                        maxDuration = 600L
                    )
                )
            ),
            taskTypeData = mapOf(
                "org.gradle.api.tasks.compile.JavaCompile" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 1200L,
                        executionCount = 3L,
                        minDuration = 300L,
                        maxDuration = 500L
                    )
                )
            )
        )
        val second = TaskDurationSummary(
            taskNameData = mapOf(
                "test" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 2000L,
                        executionCount = 2L,
                        minDuration = 900L,
                        maxDuration = 1100L
                    )
                )
            ),
            taskTypeData = mapOf(
                "org.gradle.api.tasks.testing.Test" to TaskExecutionSummary(
                    total = TaskExecutionData(
                        totalDuration = 2500L,
                        executionCount = 1L,
                        minDuration = 2500L,
                        maxDuration = 2500L
                    )
                )
            )
        )

        // When
        val result = first + second

        // Then - Should contain all keys from both summaries without merging
        assertThat(result.taskNameData.size, equalTo(2))
        assertThat(result.taskNameData["compile"]?.total?.totalDuration, equalTo(1000L))
        assertThat(result.taskNameData["test"]?.total?.totalDuration, equalTo(2000L))

        assertThat(result.taskTypeData.size, equalTo(2))
        assertThat(result.taskTypeData["org.gradle.api.tasks.compile.JavaCompile"]?.total?.totalDuration, equalTo(1200L))
        assertThat(result.taskTypeData["org.gradle.api.tasks.testing.Test"]?.total?.totalDuration, equalTo(2500L))
    }
}