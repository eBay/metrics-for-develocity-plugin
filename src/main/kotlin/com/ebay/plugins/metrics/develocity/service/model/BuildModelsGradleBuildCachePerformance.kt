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
 * The build cache performance of a Gradle build, or a report of a problem encountered.
 *
 * @param problem 
 * @param model 
 */
@Serializable

data class BuildModelsGradleBuildCachePerformance (

    @SerialName(value = "problem")
    val problem: ApiProblem? = null,

    @SerialName(value = "model")
    val model: GradleBuildCachePerformance? = null

)

