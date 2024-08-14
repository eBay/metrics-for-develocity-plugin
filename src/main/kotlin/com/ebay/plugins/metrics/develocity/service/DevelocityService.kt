package com.ebay.plugins.metrics.develocity.service

import com.ebay.plugins.metrics.develocity.service.model.Build
import com.ebay.plugins.metrics.develocity.service.model.BuildQuery
import com.ebay.plugins.metrics.develocity.service.model.BuildsQuery
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

/**
 * Simple interface to separate the API for interacting with the Develocity API from the implementation.
 */
interface DevelocityService {
    /**
     * Get a list of builds.  This call handles the pagination of the results automatically.
     *
     * https://docs.gradle.com/enterprise/api-manual/ref/2022.4.html#operation/GetBuilds
     */
    suspend fun builds(params: BuildsQuery, errorHandler: (response: HttpResponse) -> Unit = { _ -> }): Flow<Build>

    /**
     * Get build details for the build with the given ID.
     */
    suspend fun <T> build(
        buildId: String,
        params: BuildQuery,
        errorHandler: (response: HttpResponse) -> Unit = { _ -> },
        transform: (Build) -> T,
    ): T?

}