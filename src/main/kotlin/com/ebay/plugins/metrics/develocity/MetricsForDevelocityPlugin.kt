package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.projectcost.ProjectCostPlugin
import com.ebay.plugins.metrics.develocity.service.DevelocityBuildService
import com.ebay.plugins.metrics.develocity.userquery.UserQueryPlugin
import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Plugin implementation which defines tasks and configurations artifacts which are used to
 * generate aggregate metric data based upon Develocity build scans.
 */
@Suppress("unused") // False positive
internal class MetricsForDevelocityPlugin @Inject constructor(
    private val providerFactory: ProviderFactory
) : Plugin<Any> {

    override fun apply(target: Any) {
        when(target) {
            is Project -> applyProject(target)
            is Settings -> applySettings(target)
            else -> throw IllegalArgumentException("Unsupported plugin target type: ${target::class.java}")
        }
    }

    private fun applySettings(settings: Settings) {
        settings.gradle.beforeProject { project ->
            project.plugins.apply(MetricsForDevelocityPlugin::class.java)
        }

        // Auto-configure the Gradle Enterprise access if the plugin is applied and has been
        // directly configured with a server URL and/or access key.
        settings.plugins.withId("com.gradle.enterprise") {
            @Suppress("DEPRECATION") // GradleEnterpriseExtension is deprecated
            val gradleExt = settings.extensions.getByType(com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension::class.java)
            settings.gradle.afterProject { project ->
                project.plugins.withId("com.ebay.metrics-for-develocity") {
                    project.extensions.findByType(MetricsForDevelocityExtension::class.java)?.let { ext ->
                        with(ext) {
                            develocityServerUrl.convention(gradleExt.server)
                            develocityAccessKey.convention(gradleExt.accessKey)
                        }
                    }
                }
            }
        }
        settings.plugins.withId("com.gradle.develocity") {
            val gradleExt = settings.extensions.getByType(DevelocityConfiguration::class.java)
            settings.gradle.afterProject { project ->
                project.plugins.withId("com.ebay.metrics-for-develocity") {
                    project.extensions.findByType(MetricsForDevelocityExtension::class.java)?.let { ext ->
                        with(ext) {
                            develocityServerUrl.convention(gradleExt.server)
                            develocityAccessKey.convention(gradleExt.accessKey)
                        }
                    }
                }
            }
        }
    }

    private fun applyProject(project: Project) {
        if (project.parent == null) {
            applyRootProject(project)
        }

        // Example summarizers
        project.plugins.apply(ProjectCostPlugin::class.java)
        project.plugins.apply(UserQueryPlugin::class.java)
    }

    private fun applyRootProject(project: Project) {
        // Create the extension which will be used to configure the plugin behavior
        val ext = project.extensions.create(EXTENSION_NAME, MetricsForDevelocityExtension::class.java)
            .apply {
                zoneId.convention(ZoneId.systemDefault().id)
                develocityMaxConcurrency.convention(24)
                develocityQueryFilter.convention(
                    providerFactory.gradleProperty(QUERY_FILTER_PROPERTY)
                        .orElse("project:${project.name}"))
            }

        // Register the build service used to query Develocity for build data
        val buildServiceProvider = project.gradle.sharedServices.registerIfAbsent(
            "metricsForDevelocityBuildService",
            DevelocityBuildService::class.java
        ) { spec ->
            with(spec.parameters) {
                serverUrlProperty.set(ext.develocityServerUrl)
                accessKeyProperty.set(ext.develocityAccessKey)
                accessKeyFileProperty.set(project.file(project.gradle.gradleUserHomeDir).resolve("develocity/keys.properties"))
                legacyAccessKeyFileProperty.set(project.file(project.gradle.gradleUserHomeDir).resolve("enterprise/keys.properties"))
                maxConcurrency.set(ext.develocityMaxConcurrency)
            }
            /*
             * Although the build service can be configured with a higher level of concurrency,
             * we limit the number of parallel usages here to a single task.  This is because:
             * - Each task may resolve into many builds
             * - If we run more than one task at a time, the execution time of the all tasks will
             *   be artificially extended due to Develocity API concurrency contention.  We want
             *   execution times of each task to be as accurate as possible.
             * - Should the build fail or be aborted, we want the build cache to be able to avoid
             *   re-running a task.  Completing individual tasks as rapidly as possible helps to
             *   achieve this.
             */
            spec.maxParallelUsages.set(1)
        }

        // Create a "current day" value source, configured with the proper time zone
        val dateHelper = DateHelper(ext.zoneId)
        val currentDayWithHourProvider =
            providerFactory.of(CurrentDayWithHourValueSource::class.java) { spec ->
                spec.parameters { params ->
                    params.zoneId.set(ext.zoneId)
                }
            }


        // Helper to allow all of this data to be easily passed into helper functions
        val pluginContext = PluginContext(
            project,
            buildServiceProvider,
            currentDayWithHourProvider,
            dateHelper,
            ext,
        )

        /*
         * Rule to register a task for a specific date and optionally hour.
         */
        project.tasks.addRule(
            "Pattern: $TASK_PREFIX-<YYYY>-<MM>-<DD>T[<HH>]  " +
                    "Gathers Develocity metrics for the date (and optionally hour) specified."
        ) { taskName ->
            val matcher = DATETIME_TASK_PATTERN.matcher(taskName)
            if (!matcher.matches()) return@addRule

            val date = matcher.group(2)
            val timeSpec: String = matcher.group(1)
            val hour: String? = matcher.group(3)

            if (hour == null ) {
                // Daily task
                pluginContext.registerDaily(date)
            } else {
                // Hourly task
                pluginContext.registerHourly(timeSpec)
            }
        }

        /*
         * Rule to register a task for a time window / duration, relative to the current day and hour.
         */
        project.tasks.addRule(
            "Pattern: $TASK_PREFIX-last-<Java Duration String>  " +
                    "Aggregates Develocity metrics for the current date back in time for the " +
                    "specified duration."
        ) { taskName ->
            val matcher = DURATION_TASK_PATTERN.matcher(taskName)
            if (!matcher.matches()) return@addRule

            val durationStr: String = matcher.group(1)
            pluginContext.registerDurationAggregation(durationStr)
        }

        ext.extensions.create(
            INTERNAL_EXTENSION_NAME,
            MetricsForDevelocityInternalExtension::class.java,
            { timeSpec: String -> pluginContext.registerHourly(timeSpec) },
            { dateStr: String -> pluginContext.registerDaily(dateStr) },
            { durationStr: String -> pluginContext.registerDurationAggregation(durationStr) },
        )
    }

    /*
     * Function to register an hourly gather task.  This function must be re-entrant and allow for
     * the same task to be registered multiple times without error.
     */
    private fun PluginContext.registerHourly(timeSpec: String): TaskProvider<GatherHourlyTask> {
        val taskName = "$TASK_PREFIX-$timeSpec"
        return if (project.tasks.names.contains(taskName)) {
            // task already exists.  Return its TaskProvider.
            project.tasks.named(taskName, GatherHourlyTask::class.java)
        } else {
            // Task does not exist, so we need to create it.
            project.tasks.register(taskName, GatherHourlyTask::class.java).also { gatherTaskProvider ->
                gatherTaskProvider.configure { task ->
                    with(task) {
                        // Never cache the current hour so that we get up-to-date data:
                        val currentDayWithHour = currentDayWithHourProvider.get()
                        if (timeSpec == currentDayWithHour) {
                            with(outputs) {
                                cacheIf("The current hour is never cached to ensure we have all data") { false }
                                upToDateWhen { false }
                            }
                        }

                        val start = dateHelper.fromHourlyString(timeSpec)
                        startProperty.set(start.toEpochMilli())
                        endExclusiveProperty.set(
                            start.plus(1, ChronoUnit.HOURS).toEpochMilli()
                        )
                        zoneIdProperty.set(ext.zoneId)
                        queryProperty.set(ext.develocityQueryFilter)
                        summarizersProperty.set(ext.summarizers)
                        develocityServiceProperty.set(buildServiceProvider)
                        outputDirectoryProperty.set(PathUtil.hourlyOutputDir(project.layout, timeSpec))
                        usesService(buildServiceProvider)
                    }
                }
            }
        }
    }

    /*
     * Function to register an daily aggregation task.  This function must be re-entrant and allow for
     * the same task to be registered multiple times without error.
     */
    private fun PluginContext.registerDaily(dateString: String): TaskProvider<GatherAggregateTask> {
        val taskName = "$TASK_PREFIX-$dateString"
        return if (project.tasks.names.contains(taskName)) {
            // task already exists.  Return its TaskProvider.
            project.tasks.named(taskName, GatherAggregateTask::class.java)
        } else {
            // Task does not exist, so we need to create it.
            project.tasks.register(taskName, GatherAggregateTask::class.java).also { taskProvider ->
                taskProvider.configure { task ->
                    with(task) {
                        val currentDayWithHour = currentDayWithHourProvider.get()
                        val day = dateHelper.fromDailyString(dateString)
                        for (interval in 0 until 24) {
                            val hourInstant = day.plus(interval.toLong(), java.time.temporal.ChronoUnit.HOURS)
                            val timeSpec = dateHelper.toHourlyString(hourInstant)

                            dependsOn(project.provider {
                                registerHourly(timeSpec).also { hourlyProvider ->
                                    sourceOutputDirectories.from(hourlyProvider.flatMap { it.outputDirectoryProperty })
                                }.name
                            })

                            if (timeSpec == currentDayWithHour) {
                                // We are processing the current day and have reached the current hour.  Stop here.
                                break
                            }
                        }
                        zoneOffset.set(ext.zoneId)
                        summarizersProperty.set(ext.summarizers)
                        outputDirectoryProperty.set(PathUtil.dailyOutputDir(project.layout, dateString))
                    }
                }
            }
        }
    }

    /*
     * Function to register an duration aggregation task.  This function must be re-entrant and allow for
     * the same task to be registered multiple times without error.
     */
    private fun PluginContext.registerDurationAggregation(durationStr: String): TaskProvider<GatherAggregateTask> {
        val duration = Duration.parse(durationStr)
        val currentDayWithHour = currentDayWithHourProvider.get()
        val endTime = dateHelper.fromHourlyString(currentDayWithHour)
        val startTime = endTime.minus(duration).truncatedTo(ChronoUnit.HOURS)
        val inputTaskProviders = generateTimeSequence(ext.zoneId.get(), startTime, endTime).map { (isHourly, dateTime) ->
            val dateStr = dateHelper.toDailyString(dateTime.toInstant())
            if (isHourly) {
                val timeSpec = dateHelper.toHourlyString(dateTime.toInstant())
                registerHourly(timeSpec)
            } else {
                registerDaily(dateStr)
            }
        }.toList()

        val taskName = "$TASK_PREFIX-last-$durationStr"
        return if (project.tasks.names.contains(taskName)) {
            // task already exists.  Return its TaskProvider.
            project.tasks.named(taskName, GatherAggregateTask::class.java)
        } else {
            // Task does not exist, so we need to create it.
            project.tasks.register(taskName, GatherAggregateTask::class.java).also { taskProvider ->
                taskProvider.configure { task ->
                    with(task) {
                        inputTaskProviders.forEach { taskProvider ->
                            sourceOutputDirectories.from(taskProvider.flatMap {
                                if (it is MetricsIntermediateTask) {
                                    it.outputDirectoryProperty
                                } else {
                                    throw IllegalStateException("Unexpected task type: ${it::class.java}")
                                }
                            })
                        }
                        zoneOffset.set(ext.zoneId)
                        summarizersProperty.set(ext.summarizers)
                        outputDirectoryProperty.set(PathUtil.durationOutputDir(project.layout, durationStr))
                    }
                }
            }
        }
    }

    /**
     * Generates a sequence of date time instances between the start and end times.
     *
     * The sequence will return hourly intervals for the start day and end days and daily
     * intervals for everything else in between.
     *
     * If the start time is more than 7 days in the past, the sequence will return a daily
     * reference for the start of the time range, sacrificing some accuracy in order to improve
     * the chances that the data will still exist in the build cache.
     *
     * @return pair of boolean indicating if the instant is an hourly interval and the date
     *  time instance itself
     */
    private fun generateTimeSequence(timeZoneId: String, startTime: Instant, endTime: Instant) : Sequence<Pair<Boolean, ZonedDateTime>> {
        val zone = ZoneId.of(timeZoneId)
        val zonedStartTime = startTime.atZone(zone)
        val startDay = startTime.atZone(zone).truncatedTo(ChronoUnit.DAYS)
        val endDay = endTime.atZone(zone).truncatedTo(ChronoUnit.DAYS)

        // If we get this far in the past then we retain daily rollup-use, sacrificing some
        // precision at the start-end of the range but making it more likely that we won't
        // be requesting data that has been evicted from the cache.
        val dailyCutOffThreshold = endTime.atZone(zone)
            .minus(7, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.DAYS)

        return sequence {
            var dateTime = endTime.atZone(zone)
            while (dateTime >= zonedStartTime) {
                val timeDay = dateTime.truncatedTo(ChronoUnit.DAYS)
                if ((timeDay == startDay && timeDay.isAfter(dailyCutOffThreshold)) || timeDay == endDay) {
                    yield(Pair(true, dateTime.truncatedTo(ChronoUnit.HOURS)))
                    dateTime = dateTime.minus(1, ChronoUnit.HOURS)
                } else {
                    val currentDay = dateTime.truncatedTo(ChronoUnit.DAYS)
                    yield(Pair(false, currentDay))
                    val nextDay = dateTime.minus(1, ChronoUnit.DAYS)
                    dateTime = if (nextDay == startDay) {
                        // start at the end hour of the day
                        nextDay.plus(23, ChronoUnit.HOURS)
                    } else {
                        dateTime.minus(1, ChronoUnit.DAYS)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTENSION_NAME = "metricsForDevelocity"
        const val INTERNAL_EXTENSION_NAME = "metricsForDevelocityInternal"
        const val TASK_PREFIX = "metricsForDevelocity"
        const val TASK_GROUP = "develocity metrics"
        const val QUERY_FILTER_PROPERTY = "metricsForDevelocityQueryFilter"

        private val DATETIME_TASK_PATTERN = Regex(
            // Examples:
            //      metricsForDevelocity-2024-06-01
            //      metricsForDevelocity-2024-06-01T05
            "^\\Q$TASK_PREFIX-\\E((\\d{4}-\\d{2}-\\d{2})(?:T(\\d{2}))?)$"
        ).toPattern()

        private val DURATION_TASK_PATTERN = Regex(
            // Examples:
            //      metricsForDevelocity-last-P7D
            //      metricsForDevelocity-last-PT8H
            //      metricsForDevelocity-last-P2DT8H
            "^\\Q$TASK_PREFIX-last-\\E(\\w+)$"
        ).toPattern()
    }
}
