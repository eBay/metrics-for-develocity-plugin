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
 * Build model names that can be requested when fetching builds.
 *
 * Values: GRADLE_MINUS_ATTRIBUTES,GRADLE_MINUS_BUILD_MINUS_CACHE_MINUS_PERFORMANCE,GRADLE_MINUS_PROJECTS,GRADLE_MINUS_NETWORK_MINUS_ACTIVITY,GRADLE_MINUS_ARTIFACT_MINUS_TRANSFORM_MINUS_EXECUTIONS,GRADLE_MINUS_DEPRECATIONS,MAVEN_MINUS_ATTRIBUTES,MAVEN_MINUS_BUILD_MINUS_CACHE_MINUS_PERFORMANCE,MAVEN_MINUS_MODULES,MAVEN_MINUS_DEPENDENCY_MINUS_RESOLUTION
 */
@Serializable
enum class BuildModelName(val value: kotlin.String) {

    @SerialName(value = "gradle-attributes")
    GRADLE_MINUS_ATTRIBUTES("gradle-attributes"),

    @SerialName(value = "gradle-build-cache-performance")
    GRADLE_MINUS_BUILD_MINUS_CACHE_MINUS_PERFORMANCE("gradle-build-cache-performance"),

    @SerialName(value = "gradle-projects")
    GRADLE_MINUS_PROJECTS("gradle-projects"),

    @SerialName(value = "gradle-network-activity")
    GRADLE_MINUS_NETWORK_MINUS_ACTIVITY("gradle-network-activity"),

    @SerialName(value = "gradle-artifact-transform-executions")
    GRADLE_MINUS_ARTIFACT_MINUS_TRANSFORM_MINUS_EXECUTIONS("gradle-artifact-transform-executions"),

    @SerialName(value = "gradle-deprecations")
    GRADLE_MINUS_DEPRECATIONS("gradle-deprecations"),

    @SerialName(value = "maven-attributes")
    MAVEN_MINUS_ATTRIBUTES("maven-attributes"),

    @SerialName(value = "maven-build-cache-performance")
    MAVEN_MINUS_BUILD_MINUS_CACHE_MINUS_PERFORMANCE("maven-build-cache-performance"),

    @SerialName(value = "maven-modules")
    MAVEN_MINUS_MODULES("maven-modules"),

    @SerialName(value = "maven-dependency-resolution")
    MAVEN_MINUS_DEPENDENCY_MINUS_RESOLUTION("maven-dependency-resolution");

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): kotlin.String = value

    companion object {
        /**
         * Converts the provided [data] to a [String] on success, null otherwise.
         */
        fun encode(data: kotlin.Any?): kotlin.String? = if (data is BuildModelName) "$data" else null

        /**
         * Returns a valid [BuildModelName] for [data], null otherwise.
         */
        fun decode(data: kotlin.Any?): BuildModelName? = data?.let {
          val normalizedData = "$it".lowercase()
          values().firstOrNull { value ->
            it == value || normalizedData == "$value".lowercase()
          }
        }
    }
}

