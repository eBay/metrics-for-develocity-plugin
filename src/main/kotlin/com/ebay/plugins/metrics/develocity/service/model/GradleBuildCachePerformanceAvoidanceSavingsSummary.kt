/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package com.ebay.plugins.metrics.develocity.service.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * **This is deprecated, use `GradleBuildCachePerformanceTaskAvoidanceSavingsSummary` instead.** A breakdown of avoidance savings. 
 *
 * @param total The estimated reduction in serial execution time of the tasks due to their outputs being reused in milliseconds.
 * @param ratio The ratio of the total avoidance savings against the potential serial execution time (which is the actual serial execution time plus the total avoidance savings). Quantifies the effect of avoidance savings in this build. The bigger the ratio is, the more time is saved when running the build.
 * @param upToDate The estimated reduction in serial execution time of the tasks due to build incrementalism in milliseconds.
 * @param localBuildCache The estimated reduction in serial execution time of the tasks due to their outputs being reused from the local build cache in milliseconds.
 * @param remoteBuildCache The estimated reduction in serial execution time of the tasks due to their outputs being reused from the remote build cache in milliseconds.
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")

data class GradleBuildCachePerformanceAvoidanceSavingsSummary (

    /* The estimated reduction in serial execution time of the tasks due to their outputs being reused in milliseconds. */
    @SerialName(value = "total")
    val total: kotlin.Long,

    /* The ratio of the total avoidance savings against the potential serial execution time (which is the actual serial execution time plus the total avoidance savings). Quantifies the effect of avoidance savings in this build. The bigger the ratio is, the more time is saved when running the build. */
    @SerialName(value = "ratio")
    val ratio: kotlin.Double,

    /* The estimated reduction in serial execution time of the tasks due to build incrementalism in milliseconds. */
    @SerialName(value = "upToDate")
    val upToDate: kotlin.Long,

    /* The estimated reduction in serial execution time of the tasks due to their outputs being reused from the local build cache in milliseconds. */
    @SerialName(value = "localBuildCache")
    val localBuildCache: kotlin.Long,

    /* The estimated reduction in serial execution time of the tasks due to their outputs being reused from the remote build cache in milliseconds. */
    @SerialName(value = "remoteBuildCache")
    val remoteBuildCache: kotlin.Long

)

