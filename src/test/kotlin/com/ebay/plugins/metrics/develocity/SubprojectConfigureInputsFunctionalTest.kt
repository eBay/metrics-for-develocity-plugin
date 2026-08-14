package com.ebay.plugins.metrics.develocity

import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.testng.annotations.Ignore
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Files

/**
 * Covers [configureInputs] when the consuming task lives on a **subproject**.
 *
 * The producer consumable configuration is registered only via
 * [MetricsForDevelocityExtension.ensureTimeSpecConfiguration], which is backed by a
 * registrar installed exclusively on the **root** project's extension
 * ([MetricsForDevelocityProjectPlugin.applyRootProject]). Subproject
 * [configureInputs] currently skips that call (`project.parent == null`), so a
 * subproject task whose name does not contain a time-spec suffix never causes the
 * root producer configuration to be created.
 */
class SubprojectConfigureInputsFunctionalTest {

    @Test
    @Ignore("This is the situation that currently requires use of the metricsForDevelocityConfigurations property")
    fun subprojectTaskWithoutTimeSpecSuffixStillCreatesRootProducerConfiguration() {
        val projectDir = writeFixture()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(
                ":consumer:customDurationReport", // <-- Task name does not have time spec suffix
                "--dry-run",
                "--stacktrace",
            )
            .forwardOutput()
            .build()

        assertThat(
            "Root producer gather task must be in the graph so variant resolution " +
                    "can select the summarizer output (not a sibling plugin's configuration).",
            result.output,
            containsString(":metricsForDevelocity-last-P2D"),
        )
        assertThat(result.output, containsString(":consumer:customDurationReport"))
    }

    /**
     * Control: the settings plugin still infers P2D from a duration-suffixed task name,
     * which pre-creates the root producer configuration even for a subproject consumer.
     */
    @Test
    fun subprojectTaskWithTimeSpecSuffixCreatesRootProducerConfiguration() {
        val projectDir = writeFixture()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(
                ":consumer:projectCostReport-P2D",
                "--dry-run",
                "--stacktrace",
            )
            .forwardOutput()
            .build()

        assertThat(result.output, containsString(":metricsForDevelocity-last-P2D"))
        assertThat(result.output, containsString(":consumer:projectCostReport-P2D"))
    }

    private fun writeFixture(): File {
        val dir = Files.createTempDirectory("mfd-subproject-configure-inputs").toFile()
        dir.resolve("settings.gradle").writeText(
            """
            plugins {
                id 'com.ebay.metrics-for-develocity'
            }
            rootProject.name = 'subproject-configure-inputs-fixture'
            include 'consumer'
            """.trimIndent()
        )
        // Root must not call inputsFromDuration for P2D; otherwise it would hide the bug.
        dir.resolve("build.gradle").writeText("\n")
        val consumerDir = dir.resolve("consumer").apply { mkdirs() }
        consumerDir.resolve("build.gradle").writeText(
            """
            import com.ebay.plugins.metrics.develocity.MetricSummarizerTask
            import com.ebay.plugins.metrics.develocity.TaskProviderExtensionsKt

            abstract class CustomSummarizerTask extends DefaultTask implements MetricSummarizerTask {}

            def custom = tasks.register('customDurationReport', CustomSummarizerTask)
            TaskProviderExtensionsKt.inputsFromDuration(custom, project, 'P2D', 'projectCost')
            """.trimIndent()
        )
        return dir
    }
}
