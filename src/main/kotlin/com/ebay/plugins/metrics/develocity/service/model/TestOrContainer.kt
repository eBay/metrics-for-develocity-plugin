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
 * A test or test container.
 *
 * @param name The name of the test or test container.
 * @param outcomeDistribution 
 * @param workUnits 
 * @param buildScanIdsByOutcome 
 */
@Serializable

data class TestOrContainer (

    /* The name of the test or test container. */
    @SerialName(value = "name")
    val name: kotlin.String,

    @SerialName(value = "outcomeDistribution")
    val outcomeDistribution: TestOutcomeDistribution,

    @SerialName(value = "workUnits")
    val workUnits: kotlin.collections.List<TestWorkUnit>? = null,

    @SerialName(value = "buildScanIdsByOutcome")
    val buildScanIdsByOutcome: BuildScanIdsByOutcome? = null

)

