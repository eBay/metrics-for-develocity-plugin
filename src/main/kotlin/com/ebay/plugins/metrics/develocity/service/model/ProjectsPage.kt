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
 * A paged list of projects.
 *
 * @param content A list of projects.
 * @param page 
 */
@Serializable

data class ProjectsPage (

    /* A list of projects. */
    @SerialName(value = "content")
    val content: kotlin.collections.List<Project>,

    @SerialName(value = "page")
    val page: PageMetadata

)

