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
 * 
 *
 * @param string The complete version string of format `YEAR.RELEASE.PATCH`, where the patch component is omitted when `0` (e.g. `2022.3`, `2022.3.1`).
 * @param year The gregorian calendar year of the release.
 * @param release The sequence number of the release for that year, starting with 1.
 * @param patch The patch level of the release, starting with 0.
 */
@Serializable

data class DevelocityVersion (

    /* The complete version string of format `YEAR.RELEASE.PATCH`, where the patch component is omitted when `0` (e.g. `2022.3`, `2022.3.1`). */
    @SerialName(value = "string")
    val string: kotlin.String,

    /* The gregorian calendar year of the release. */
    @SerialName(value = "year")
    val year: kotlin.Int,

    /* The sequence number of the release for that year, starting with 1. */
    @SerialName(value = "release")
    val release: kotlin.Int,

    /* The patch level of the release, starting with 0. */
    @SerialName(value = "patch")
    val patch: kotlin.Int

)

