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
 * A project.
 *
 * @param id The unique identifier for the project. Must not contain whitespace.
 * @param displayName The label used when displaying the project.
 * @param description The description of the project group.
 */
@Serializable

data class Project (

    /* The unique identifier for the project. Must not contain whitespace. */
    @SerialName(value = "id")
    val id: kotlin.String,

    /* The label used when displaying the project. */
    @SerialName(value = "displayName")
    val displayName: kotlin.String? = null,

    /* The description of the project group. */
    @SerialName(value = "description")
    val description: kotlin.String? = null

)

