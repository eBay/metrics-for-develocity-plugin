package com.ebay.plugins.metrics.develocity.userquery

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
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * [MetricSummarizer] implementation which collects the usernames of the builds that match the
 * query filter.
 */
class UserQuerySummarizer(
    private val compressOutput: Boolean = false, // For testing, manually set to false to see the rendered JSON
): MetricSummarizer<UserQuerySummary>() {
    override val id = ID
    override val modelsNeeded = setOf(
        BuildModelName.gradleAttributes,
    )

    private val serializer by lazy {
        UserQuerySummary.serializer()
    }

    @OptIn(ExperimentalSerializationApi::class) // decodeFromStream
    override fun read(file: File): UserQuerySummary {
        return if (file.exists()) {
            file.inputStream().use { inputStream ->
                BufferedInputStream(inputStream).use { buffered ->
                    if (compressOutput) {
                        GZIPInputStream(buffered).use { gzip ->
                            Json.decodeFromStream(serializer, gzip)
                        }
                    } else {
                        prettyJson.decodeFromStream(serializer, buffered)
                    }
                }
            }
        } else {
            UserQuerySummary()
        }
    }

    @OptIn(ExperimentalSerializationApi::class) // encodeToStream
    override fun write(intermediate: UserQuerySummary, file: File) {
        file.outputStream().use { outputStream ->
            BufferedOutputStream(outputStream).use { buffered ->
                if (compressOutput) {
                    GZIPOutputStream(buffered).use { gzip ->
                        Json.encodeToStream(serializer, intermediate, gzip)
                    }
                } else {
                    prettyJson.encodeToStream(serializer, intermediate, buffered)
                }
            }
        }
    }

    override fun extract(build: Build): UserQuerySummary {
        val userName = build.models?.gradleAttributes?.model?.environment?.username ?: return UserQuerySummary()
        return UserQuerySummary(
            users = setOf(userName),
        )
    }

    override fun reduce(left: UserQuerySummary, right: UserQuerySummary): UserQuerySummary {
        return UserQuerySummary(
            users = left.users + right.users,
        )
    }

    companion object {
        const val ID = "userQuery"

        @OptIn(ExperimentalSerializationApi::class)
        val prettyJson = Json { // this returns the JsonBuilder
            prettyPrint = true
            prettyPrintIndent = " "
        }
    }
}