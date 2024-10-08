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
 * A Build Cache Node key and secret pair.
 *
 * @param key A unique identifier for the Build Cache Node in Develocity.
 * @param secret The secret associated with the Build Cache Node.
 */
@Serializable

data class KeySecretPair (

    /* A unique identifier for the Build Cache Node in Develocity. */
    @SerialName(value = "key")
    val key: kotlin.String,

    /* The secret associated with the Build Cache Node. */
    @SerialName(value = "secret")
    val secret: kotlin.String

)

