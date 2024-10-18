package com.ebay.plugins.metrics.develocity.configcachemiss

import com.ebay.plugins.metrics.develocity.MetricSummarizerTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import javax.inject.Inject

/**
 * Create a report of all config cache miss reasons, sorted by frequency.
 */
internal abstract class ConfigCacheMissReportTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask(), MetricSummarizerTask {
    @get:Option(option = "pattern", description = "Pattern to consolidate/mutate the cache miss reasons")
    @get:Input
    val patterns: ListProperty<String> = objectFactory.listProperty(String::class.java)
        .convention(listOf(
            /*
             * The following two patterns consolidate Intellij/AndroidStudio init script injections
             * such as the following, into a single reason string:
             *
             * content of 2nd init script, '../../../../../private/var/folders/bn/66lrv12s3yx1hk_r_92r3fch0000gp/T/ijresolvers19.gradle', has changed
             */
            "(content of )[^ ]+ (init script)",
            "\\.\\./[^ ]*(ijresolvers)[0-9]*(.gradle)",
        ))

    /**
     * The output directory where the summarizer results should be stored.
     */
    @get:OutputFile
    val reportFile: RegularFileProperty = objectFactory.fileProperty()

    @TaskAction
    fun createReport() {
        val summaryFile = summarizerDataProperty.get()
        val model = ConfigCacheMissSummarizer().read(summaryFile)
        val regexes = patterns.get().map { it.toRegex() }
        val reducedMap = reduceMap(model.reasons, regexes)
        val report = reducedMap.map { entry ->
                Pair(entry.value, entry.key)
            }.sortedByDescending { (count, _) ->
                count
            }.joinToString(
                prefix = "Config cache miss reason(s):\n    ",
                separator = "\n    ",
            ) { (count, reason) ->
                "%5d: %s".format(count, reason)
            }.replace("\n", System.lineSeparator())

        reportFile.asFile.get().also { reportFile ->
            logger.lifecycle("Config cache miss report available at: file://${reportFile.absolutePath}")
            reportFile.writeText(report)
        }
    }

    companion object {
        /**
         * (Potentially) reduces the map of reasons by running the reasons through a filter
         * of regular expressions.  This allows multiple similar reasons to be consolidated
         * into a single reason for consumption purposes.  See [reduceKey] for more details
         * on the consolidation logic.
         */
        internal fun reduceMap(originalMap: Map<String, Int>, regexes: List<Regex>): Map<String, Int> {
            val reducedMap = mutableMapOf<String, Int>()
            originalMap.forEach { (reason, count) ->
                reducedMap.compute(reduceKey(reason, regexes)) { _, existingCount ->
                    (existingCount ?: 0) + count
                }
            }
            return reducedMap.toMap()
        }

        /**
         * Determines a new key value by running the provided key value through a filtering process.
         * The filtering is performed by applying a list of regular expressions.
         *
         * The behavior of these regular expressions is broken into two categories; those that
         * define capturing groups and those that do not.
         *
         * For regular expressions which do not define capturing groups, the any matches within
         * the key are removed from the key.
         *
         * For regular expressions which do define capturing groups, the matched portion of the
         * key is replaced with a concatenation of all defined capturing group values.  This
         * allows portions of the match to be retained.
         */
        internal fun reduceKey(originalKey: String, regexes: List<Regex>): String {
            if (regexes.isEmpty()) {
                return originalKey
            }

            var newKey = originalKey
            regexes.forEach { regex ->
                while(true) {
                    val matchResult = regex.find(newKey) ?: break
                    val nextKey = if (matchResult.groups.size == 1) {
                        // Only one group (0) so assume we want to strip these matches.
                        newKey.removeRange(matchResult.range)
                    } else {
                        // Multiple groups match.  Concatenate groups >= 1.
                        val replacement = matchResult.groups.drop(1).joinToString(separator = "") { matchGroup ->
                            matchGroup?.value ?: ""
                        }
                        newKey.replaceRange(matchResult.range, replacement)
                    }
                    if (nextKey == newKey) {
                        break
                    } else {
                        newKey = nextKey
                    }
                }
            }
            return newKey
        }
    }
}