package com.ebay.plugins.metrics.develocity.projectcost

import com.ebay.plugins.graph.analytics.BaseGraphInputOutputTask
import com.ebay.plugins.graph.analytics.EdgeInfo
import com.ebay.plugins.graph.analytics.VertexInfo
import kotlinx.serialization.json.Json
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute

/**
 * Graph Analysis task which consumes the project cost report and integrates it into the
 * project graph.
 */
@CacheableTask
internal abstract class ProjectCostGraphAnalysisTask : BaseGraphInputOutputTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val projectReportProperty: RegularFileProperty

    override fun processGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>) {
        val projectCostReportFile = projectReportProperty.get()
        val model = Json.decodeFromString(
            ProjectCostReport.serializer(),
            projectCostReportFile.asFile.readText()
        )
        graph.vertexSet().forEach { vertexInfo ->
            model.projectData[vertexInfo.path]?.let { report ->
                vertexInfo.attributes["buildAvgDuration"] = DefaultAttribute(report.buildAvgDuration, AttributeType.LONG)
                vertexInfo.attributes["buildAvgTasks"] = DefaultAttribute(report.buildAvgTasks, AttributeType.LONG)
                vertexInfo.attributes["buildCount"] = DefaultAttribute(report.buildCount, AttributeType.INT)
                vertexInfo.attributes["buildCostScalar"] = DefaultAttribute(report.buildCostScalar, AttributeType.LONG)
                vertexInfo.attributes["buildDuration"] = DefaultAttribute(report.buildDuration, AttributeType.LONG)
                vertexInfo.attributes["buildPercentage"] = DefaultAttribute(report.buildPercentage, AttributeType.FLOAT)
                vertexInfo.attributes["buildImpactedUsers"] = DefaultAttribute(report.impactedUserCount, AttributeType.INT)
            }
        }
    }
}
