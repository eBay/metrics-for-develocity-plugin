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
 * Information about the build cache overhead in this build.
 *
 * @param uploading Overhead of upload operations in milliseconds.
 * @param downloading Overhead of download operations in milliseconds.
 * @param packing Overhead of pack operations in milliseconds.
 * @param unpacking Overhead of unpack operations in milliseconds.
 */
@Serializable

data class MavenBuildCachePerformanceBuildCacheOverhead (

    /* Overhead of upload operations in milliseconds. */
    @SerialName(value = "uploading")
    val uploading: kotlin.Long,

    /* Overhead of download operations in milliseconds. */
    @SerialName(value = "downloading")
    val downloading: kotlin.Long,

    /* Overhead of pack operations in milliseconds. */
    @SerialName(value = "packing")
    val packing: kotlin.Long,

    /* Overhead of unpack operations in milliseconds. */
    @SerialName(value = "unpacking")
    val unpacking: kotlin.Long

)

