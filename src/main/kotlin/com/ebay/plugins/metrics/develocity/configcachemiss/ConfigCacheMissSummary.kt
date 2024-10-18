package com.ebay.plugins.metrics.develocity.configcachemiss

import kotlinx.serialization.Serializable

/**
 * Intermediate data model used to aggregate a set of reasons for cache misses along with their
 * observed counts.
 */
@Serializable
data class ConfigCacheMissSummary(
    val reasons: Map<String, Int> = emptyMap(),
)