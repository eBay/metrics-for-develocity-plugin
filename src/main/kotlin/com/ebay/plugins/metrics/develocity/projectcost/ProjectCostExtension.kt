package com.ebay.plugins.metrics.develocity.projectcost

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty

/**
 * Gradle extension used to configure the [ProjectCostExtension].
 */
abstract class ProjectCostExtension : ExtensionAware {
    /**
     * The list of task names which should be ignored when determining if a project has been "executed" in a
     * build.  This allows for the exclusion of tasks that always run and skew the resulting data from the
     * perspective of gaining insights into what projects are actually doing work within a build.
     */
    abstract val taskNamesIgnoredForExecutionDetermination: ListProperty<String>
}