package com.ebay.plugins.metrics.develocity.service

import com.gabrielfeo.develocity.api.Config
import com.gabrielfeo.develocity.api.DevelocityApi
import com.gabrielfeo.develocity.api.extension.getBuildsFlow
import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.BuildQuery
import com.gabrielfeo.develocity.api.model.BuildsQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.BuildService
import java.net.URI
import java.util.Properties
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

/**
 * Gradle shared build service implementation which provides access to the Develocity API.
 *
 * This allows for the HTTP client to be centrally managed, centrally managing request
 * parallelism.
 */
abstract class DevelocityBuildService @Inject constructor(
    private val providerFactory: ProviderFactory
): BuildService<DevelocityBuildServiceParameters>, DevelocityService, AutoCloseable {
    private val serverUrl by lazy { resolveServer() }

    private val accessKey by lazy { resolveAccessKey(serverUrl) }

    private val baseUrl = serverUrl.removeSuffix("/").plus("/api/")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher by lazy {
        Dispatchers.IO.limitedParallelism(parameters.maxConcurrency.get())
    }

    private val api by lazy {
        DevelocityApi.newInstance(Config(
            apiUrl = baseUrl,
            apiToken = ::accessKey,
            maxConcurrentRequests = parameters.maxConcurrency.get(),
            logLevel = "debug",
            readTimeoutMillis = 5.minutes.inWholeMilliseconds,
        ))
    }

    override fun close() {
        api.shutdown()
    }

    override suspend fun builds(
        params: BuildsQuery,
    ): Flow<Build> = withContext(dispatcher) {
        val flow = api.buildsApi.getBuildsFlow(
            fromInstant = params.fromInstant,
            fromBuild = params.fromBuild,
            reverse = params.reverse,
            maxWaitSecs = params.maxWaitSecs,
            query = params.query,
            models = params.models,
            allModels = params.allModels,
        )
        params.maxBuilds?.let { maxBuilds ->
            flow.take(maxBuilds)
        } ?: flow
    }

    override suspend fun <T> build(
        buildId: String,
        params: BuildQuery,
        transform: (Build) -> T,
    ): T? = withContext(dispatcher) {
        transform.invoke(api.buildsApi.getBuild(
            id = buildId,
            models = params.models,
            allModels = params.allModels,
        ))
    }

    private fun resolveServer(): String {
        return parameters.serverUrlProperty.orNull ?: throw GradleException("Develocity server URL must be set")
    }

    private fun resolveAccessKey(serverUrl: String): String {
        val host = URI(serverUrl).host
        val token = sequence {
            yield(parameters.accessKeyProperty.orNull)
            yield(getTokenFromEnvVar("DEVELOCITY_ACCESS_KEY", host))
            yield(getTokenFromEnvVar("GRADLE_ENTERPRISE_ACCESS_KEY", host))
            yield(getTokenFromFile(parameters.accessKeyFileProperty, host))
            yield(getTokenFromFile(parameters.legacyAccessKeyFileProperty, host))
        }.filterNotNull().firstOrNull()

        if (token.isNullOrBlank()) {
            throw GradleException("Unable to resolve the Develocity access key for host: $host")
        }

        return token
    }

    // xref: https://docs.gradle.com/develocity/gradle-plugin/current/#via_environment_variable
    private fun getTokenFromEnvVar(varName: String, host: String): String? {
        providerFactory.environmentVariable(varName).orNull?.let { envVar ->
            return envVar.split(';')
                .firstNotNullOfOrNull { hostEqToken ->
                    val split = hostEqToken.split('=')
                    if (split.size == 2) {
                        val (tokenHost, token) = split
                        if (tokenHost == host) {
                            token
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
        }
        return null
    }

    private fun getTokenFromFile(fileProperty: RegularFileProperty, host: String): String? {
        val file = fileProperty.get().asFile
        if (file.exists()) {
            val props = Properties()
            file.reader().use { reader ->
                props.load(reader)
            }

            return props.getProperty(host) ?: null
        }
        return null
    }
}