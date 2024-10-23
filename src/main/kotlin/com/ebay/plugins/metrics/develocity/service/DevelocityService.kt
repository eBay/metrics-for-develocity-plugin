package com.ebay.plugins.metrics.develocity.service

import com.gabrielfeo.develocity.api.model.Build
import com.gabrielfeo.develocity.api.model.BuildQuery
import com.gabrielfeo.develocity.api.model.BuildsQuery
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
    suspend fun builds(params: BuildsQuery): Flow<Build>

    /**
     * Get build details for the build with the given ID.
     */
    suspend fun build(
        buildId: String,
        params: BuildQuery,
    ): Build?
}
