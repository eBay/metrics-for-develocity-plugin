package com.ebay.plugins.metrics.develocity.service

import com.ebay.plugins.metrics.develocity.service.model.Build
import com.ebay.plugins.metrics.develocity.service.model.BuildQuery
import com.ebay.plugins.metrics.develocity.service.model.BuildsQuery
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.BuildService
import java.net.URI
import java.util.Properties
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * Gradle shared build service implementation which provides access to the Develocity API.
 *
 * This allows for the HTTP client to be centrally managed, centrally mananaging request
 * parallelism.
 */
abstract class DevelocityBuildService @Inject constructor(
    private val providerFactory: ProviderFactory
): BuildService<DevelocityBuildServiceParameters>, DevelocityService, AutoCloseable {
    private val logger = Logging.getLogger(javaClass.simpleName)

    private val serverUrl by lazy { resolveServer() }

    private val accessKey by lazy { resolveAccessKey(serverUrl) }

    private val baseUrl = serverUrl.removeSuffix("/")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher by lazy {
        Dispatchers.IO.limitedParallelism(parameters.maxConcurrency.get())
    }

    private val client by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    dispatcher(Dispatcher().apply {
                        // This controls the total concurrency of processing samples
                        maxRequestsPerHost = parameters.maxConcurrency.get()
                    })
                    connectTimeout(5.minutes.toJavaDuration())
                    readTimeout(5.minutes.toJavaDuration())
                    writeTimeout(5.minutes.toJavaDuration())
                }
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(accessKey, accessKey)
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    override fun close() {
        client.close()
    }

    override suspend fun builds(
        params: BuildsQuery,
        errorHandler: (response: HttpResponse) -> Unit
    ): Flow<Build> = withContext(dispatcher) {
        return@withContext flow {
            val response = client.get("$baseUrl/api/builds") {
                url {
                    params.fromInstant?.let { parameters.append("fromInstant", it.toString()) }
                    params.fromBuild?.let { parameters.append("fromBuild", it) }
                    params.reverse?.let { parameters.append("reverse", it.toString()) }
                    params.maxBuilds?.let { parameters.append("maxBuilds", it.toString()) }
                    params.maxWaitSecs?.let { parameters.append("maxWaitSecs", it.toString()) }
                    params.query?.let { parameters.append("query", it) }
                    params.models?.let { modelName -> parameters.appendAll("models", modelName.map { it.value }) }
                    params.allModels?.let { parameters.append("allModels", it.toString()) }
                }
            }
            logResponse(response)
            if (response.status.isSuccess()) {
                val builds: List<Build> = response.body()
                logger.debug("\t${builds.size} build(s) returned")
                builds.forEach { emit(it) }
                params.maxBuilds?.let { max ->
                    if (builds.size == max) {
                        emitAll(builds(params.copy(fromBuild = builds.last().id), errorHandler))
                    }
                }
            } else {
                errorHandler.invoke(response)
            }
        }
    }

    override suspend fun <T> build(
        buildId: String,
        params: BuildQuery,
        errorHandler: (response: HttpResponse) -> Unit,
        transform: (Build) -> T,
    ): T? = withContext(dispatcher) {
        val response = client.get("$baseUrl/api/builds/$buildId") {
            url {
                params.models?.let { modelName -> parameters.appendAll("models", modelName.map { it.value }) }
                params.allModels?.let { parameters.append("allModels", it.toString()) }
            }
        }
        logResponse(response)
        return@withContext if (response.status.isSuccess()) {
            transform.invoke(response.body())
        } else {
            errorHandler.invoke(response)
            null
        }
    }

    private fun logResponse(response: HttpResponse) {
        logger.info(
            "HTTP {} {} {}",
            response.request.method.value,
            response.request.url,
            response.status
        )
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