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
 * A build with common attributes.
 *
 * @param id The Build Scan ID.
 * @param availableAt A unix-epoch-time in milliseconds referring to the instant that Develocity completed receiving and processing the build.
 * @param buildToolType The build tool type of the build.
 * @param buildToolVersion The build tool version used.
 * @param buildAgentVersion The build agent version used.
 * @param models 
 */
@Serializable

data class Build (

    /* The Build Scan ID. */
    @SerialName(value = "id")
    val id: kotlin.String,

    /* A unix-epoch-time in milliseconds referring to the instant that Develocity completed receiving and processing the build. */
    @SerialName(value = "availableAt")
    val availableAt: kotlin.Long,

    /* The build tool type of the build. */
    @SerialName(value = "buildToolType")
    val buildToolType: kotlin.String,

    /* The build tool version used. */
    @SerialName(value = "buildToolVersion")
    val buildToolVersion: kotlin.String,

    /* The build agent version used. */
    @SerialName(value = "buildAgentVersion")
    val buildAgentVersion: kotlin.String,

    @SerialName(value = "models")
    val models: BuildModels? = null

)

