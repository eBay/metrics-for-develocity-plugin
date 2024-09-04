package com.ebay.plugins.metrics.develocity.userquery

import kotlinx.serialization.Serializable

/**
 * Intermediate data model used to aggregate the set of users who have run builds.
 */
@Serializable
data class UserQuerySummary(
    val users: Set<String> = emptySet()
)