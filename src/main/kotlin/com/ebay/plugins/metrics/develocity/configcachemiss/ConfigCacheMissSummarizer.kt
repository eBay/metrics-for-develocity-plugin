package com.ebay.plugins.metrics.develocity.configcachemiss

import com.ebay.plugins.metrics.develocity.MetricSummarizer
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.BuildModelName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File

/**
 * [MetricSummarizer] implementation which collects configuration cache miss reason data.
 */
class ConfigCacheMissSummarizer: MetricSummarizer<ConfigCacheMissSummary>() {
    override val id = ID
    override val modelsNeeded = setOf(
        BuildModelName.gradleConfigurationCache,
    )

    private val serializer by lazy {
        ConfigCacheMissSummary.serializer()
    }

    @OptIn(ExperimentalSerializationApi::class) // decodeFromStream
    override fun read(file: File): ConfigCacheMissSummary {
        return if (file.exists()) {
            file.inputStream().use { inputStream ->
                BufferedInputStream(inputStream).use { buffered ->
                    prettyJson.decodeFromStream(serializer, buffered)
                }
            }
        } else {
            ConfigCacheMissSummary()
        }
    }

    @OptIn(ExperimentalSerializationApi::class) // encodeToStream
    override fun write(intermediate: ConfigCacheMissSummary, file: File) {
        file.outputStream().use { outputStream ->
            BufferedOutputStream(outputStream).use { buffered ->
                prettyJson.encodeToStream(serializer, intermediate, buffered)
            }
        }
    }

    override fun extract(build: Build): ConfigCacheMissSummary {
        val observations: MutableMap<String, Int> = mutableMapOf()
        build.models?.gradleConfigurationCache?.model?.result?.missReasons?.forEach { reason ->
            observations.compute(reason) { _, count -> (count ?: 0) + 1 }
        }
        return ConfigCacheMissSummary(
            reasons = observations,
        )
    }

    override fun reduce(left: ConfigCacheMissSummary, right: ConfigCacheMissSummary): ConfigCacheMissSummary {
        val observations = left.reasons.toMutableMap()
        right.reasons.forEach { (reason, count) ->
            observations.compute(reason) { _, currentCount -> (currentCount ?: 0) + count }
        }
        return ConfigCacheMissSummary(
            reasons = observations,
        )
    }

    companion object {
        const val ID = "configCacheMissReasons"

        @OptIn(ExperimentalSerializationApi::class)
        val prettyJson = Json { // this returns the JsonBuilder
            prettyPrint = true
            prettyPrintIndent = " "
        }
    }
}