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
 * **This property is deprecated, use `develocitySettings` instead.** Settings for Develocity. 
 *
 * @param backgroundPublicationEnabled Indicates whether background Build Scan publication is enabled for the build. `null` if Develocity Maven extension version is < `1.6`. See https://gradle.com/help/maven-extension-configuring-background-uploading.
 * @param buildOutputCapturingEnabled Indicates whether to capture build logging output for the build. `null` if Develocity Maven extension version is < `1.11`. See https://gradle.com/help/maven-extension-capturing-build-and-test-outputs.
 * @param goalInputsFileCapturingEnabled Indicates whether to capture goal input file snapshots for the build. `null` if Develocity Maven extension version is < `1.1`. See https://gradle.com/help/maven-extension-capturing-goal-input-files.
 * @param testOutputCapturingEnabled Indicates whether to capture test logging output for the build. `null` if Develocity Maven extension version is < `1.11`. See https://gradle.com/help/maven-extension-capturing-build-and-test-outputs.
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")

data class MavenGradleEnterpriseSettings (

    /* Indicates whether background Build Scan publication is enabled for the build. `null` if Develocity Maven extension version is < `1.6`. See https://gradle.com/help/maven-extension-configuring-background-uploading. */
    @SerialName(value = "backgroundPublicationEnabled")
    val backgroundPublicationEnabled: kotlin.Boolean? = null,

    /* Indicates whether to capture build logging output for the build. `null` if Develocity Maven extension version is < `1.11`. See https://gradle.com/help/maven-extension-capturing-build-and-test-outputs. */
    @SerialName(value = "buildOutputCapturingEnabled")
    val buildOutputCapturingEnabled: kotlin.Boolean? = null,

    /* Indicates whether to capture goal input file snapshots for the build. `null` if Develocity Maven extension version is < `1.1`. See https://gradle.com/help/maven-extension-capturing-goal-input-files. */
    @SerialName(value = "goalInputsFileCapturingEnabled")
    val goalInputsFileCapturingEnabled: kotlin.Boolean? = null,

    /* Indicates whether to capture test logging output for the build. `null` if Develocity Maven extension version is < `1.11`. See https://gradle.com/help/maven-extension-capturing-build-and-test-outputs. */
    @SerialName(value = "testOutputCapturingEnabled")
    val testOutputCapturingEnabled: kotlin.Boolean? = null

)

