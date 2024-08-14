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
 * **This class is deprecated. Migrate to pool-specific agent registration keys instead.** Optional description of a Test Distribution agent API key. 
 *
 * @param description Description of the API key, such as the agent pool where it will be used, to help identify it later.
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")

data class TestDistributionApiKeyDescription (

    /* Description of the API key, such as the agent pool where it will be used, to help identify it later. */
    @SerialName(value = "description")
    val description: kotlin.String? = null

)

