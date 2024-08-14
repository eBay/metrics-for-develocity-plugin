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
 * A group of projects that can be assigned to users.
 *
 * @param id The unique identifier for the project group. Must not contain whitespace.
 * @param displayName The label used when displaying the project group.
 * @param description The description of the project group.
 * @param identityProviderAttributeValue The value of an identity provider attribute this project group is associated with. Users who have this value in the identity provider attribute will be assigned this project group. 
 * @param projects 
 */
@Serializable

data class ProjectGroup (

    /* The unique identifier for the project group. Must not contain whitespace. */
    @SerialName(value = "id")
    val id: kotlin.String,

    /* The label used when displaying the project group. */
    @SerialName(value = "displayName")
    val displayName: kotlin.String? = null,

    /* The description of the project group. */
    @SerialName(value = "description")
    val description: kotlin.String? = null,

    /* The value of an identity provider attribute this project group is associated with. Users who have this value in the identity provider attribute will be assigned this project group.  */
    @SerialName(value = "identityProviderAttributeValue")
    val identityProviderAttributeValue: kotlin.String? = null,

    @SerialName(value = "projects")
    val projects: kotlin.collections.List<ProjectReference>? = null

)

