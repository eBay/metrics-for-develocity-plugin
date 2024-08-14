package com.ebay.plugins.metrics.develocity

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Utility to help consolidate all path-related logic for the develocity metrics plugin.
 */
object PathUtil {
    private fun summarizerFileName(summarizer: DevelocityMetricSummarizer<*>) = summarizer.id

    /**
     * The base directory of all daily tasks.
     */
    private fun dailyBaseDir(
        projectLayout: ProjectLayout,
    ): Provider<Directory> = projectLayout.buildDirectory.dir("$BASE_DIR/daily")

    /**
     * The output directory for a daily task.
     */
    fun dailyOutputDir(
        projectLayout: ProjectLayout,
        dateStr: String,
    ): Provider<Directory> = dailyBaseDir(projectLayout).map { dailyBaseDir ->
            dailyBaseDir.dir(dateStr)
        }

    /**
     * The base directory for all hourly outputs.
     */
    private fun hourlyBaseDir(
        projectLayout: ProjectLayout,
    ): Provider<Directory> = projectLayout.buildDirectory.dir("$BASE_DIR/hourly")

    /**
     * The output directory for an hourly task with the given time spec.
     */
    fun hourlyOutputDir(
        projectLayout: ProjectLayout,
        timeSpec: String,
    ): Provider<Directory> = hourlyBaseDir(projectLayout).map { hourlyBaseDir ->
        hourlyBaseDir.dir(timeSpec)
    }

    /**
     * The output file for a summarizer's intermediate data file within an hourly task's
     * output directory.
     */
    fun hourlySummarizerOutputFile(
        hourlyDir: Directory,
        summarizer: DevelocityMetricSummarizer<*>,
    ): RegularFile = summarizerFile(hourlyDir, summarizer)

    /**
     * The base directory for all duration-specific tasks.
     */
    private fun durationBaseDir(
        projectLayout: ProjectLayout,
    ): Provider<Directory> = projectLayout.buildDirectory.dir("$BASE_DIR/last")

    /**
     * The output directory for a duration-specific task with the given duration spec.
     */
    fun durationOutputDir(
        projectLayout: ProjectLayout,
        durationSpec: String,
    ): Provider<Directory> = durationBaseDir(projectLayout).map { durationBaseDir ->
        durationBaseDir.dir(durationSpec)
    }

    /**
     * The output file location for a summarizer's intermediate data file within an
     * output directory.
     */
    fun summarizerFile(
        directory: Directory,
        summarizer: DevelocityMetricSummarizer<*>
    ): RegularFile = directory.file(summarizerFileName(summarizer))

    /**
     * The output file location for a summarizer's intermediate data file within an
     * output directory.
     */
    fun summarizerFile(
        directory: DirectoryProperty,
        summarizerId: String
    ): Provider<RegularFile> = directory.file(summarizerId)

    private const val BASE_DIR = "develocity-metrics"
}
