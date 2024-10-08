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
 * Settings for Develocity.
 *
 * @param backgroundPublicationEnabled Indicates whether background Build Scan publication is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `3.4`. See https://gradle.com/help/gradle-plugin-configuring-background-uploading.
 * @param buildOutputCapturingEnabled Indicates whether build logging output capturing is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `3.7`. See https://gradle.com/help/gradle-plugin-capturing-build-and-test-outputs.
 * @param fileFingerprintCapturingEnabled Indicates whether file fingerprint capturing is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `2.1`. See https://docs.gradle.com/enterprise/gradle-plugin/#capturing_task_input_files.
 * @param taskInputsFileCapturingEnabled Indicates whether task input file snapshots capturing is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `2.1`. See https://gradle.com/help/gradle-plugin-capturing-task-input-files. **This property is deprecated, use `fileFingerprintCapturingEnabled` instead.** 
 * @param testOutputCapturingEnabled Indicates whether test logging output capturing is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `3.7`. See https://gradle.com/help/gradle-plugin-capturing-build-and-test-outputs.
 */
@Serializable

data class GradleDevelocitySettings (

    /* Indicates whether background Build Scan publication is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `3.4`. See https://gradle.com/help/gradle-plugin-configuring-background-uploading. */
    @SerialName(value = "backgroundPublicationEnabled")
    val backgroundPublicationEnabled: kotlin.Boolean? = null,

    /* Indicates whether build logging output capturing is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `3.7`. See https://gradle.com/help/gradle-plugin-capturing-build-and-test-outputs. */
    @SerialName(value = "buildOutputCapturingEnabled")
    val buildOutputCapturingEnabled: kotlin.Boolean? = null,

    /* Indicates whether file fingerprint capturing is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `2.1`. See https://docs.gradle.com/enterprise/gradle-plugin/#capturing_task_input_files. */
    @SerialName(value = "fileFingerprintCapturingEnabled")
    val fileFingerprintCapturingEnabled: kotlin.Boolean? = null,

    /* Indicates whether task input file snapshots capturing is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `2.1`. See https://gradle.com/help/gradle-plugin-capturing-task-input-files. **This property is deprecated, use `fileFingerprintCapturingEnabled` instead.**  */
    @SerialName(value = "taskInputsFileCapturingEnabled")
    @Deprecated(message = "This property is deprecated.")
    val taskInputsFileCapturingEnabled: kotlin.Boolean? = null,

    /* Indicates whether test logging output capturing is enabled for the build. `null` if Gradle version is < `5.0` or Develocity Gradle plugin version is < `3.7`. See https://gradle.com/help/gradle-plugin-capturing-build-and-test-outputs. */
    @SerialName(value = "testOutputCapturingEnabled")
    val testOutputCapturingEnabled: kotlin.Boolean? = null

)

