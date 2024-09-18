package com.ebay.plugins.metrics.develocity

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Task implementation which aggregates the metric data from multiple sources and produces
 * a reduced set of intermediate data.
 */
@CacheableTask
open class GatherAggregateTask @Inject constructor(
    private val objectFactory: ObjectFactory,
): DefaultTask(), MetricsIntermediateTask {
    /**
     * The set of source directories to aggregate.  Each directory added to this collection
     * should be an output directory of a [MetricsIntermediateTask] instance.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    val sourceOutputDirectories: ConfigurableFileCollection = objectFactory.fileCollection()

    /**
     * Time zone offset to use when processing the metric data.  This is here only to ensure that
     * cached data is invalidated if the confiugure time zone changes.
     */
    @get:Input
    val zoneOffset: Property<String> = objectFactory.property(String::class.java)

    @get:Internal
    override val summarizersProperty: ListProperty<MetricSummarizer<*>> = objectFactory.listProperty(MetricSummarizer::class.java)

    @get:OutputDirectory
    override val outputDirectoryProperty: DirectoryProperty = objectFactory.directoryProperty()

    @TaskAction
    fun gather() {
        val inputs = sourceOutputDirectories.files
            .sorted()
            .joinToString(prefix = "Aggregating sources:\n\t", separator = "\n\t") { it.name }
            .replace("\n", System.lineSeparator())
        outputDirectoryProperty.file("inputs").get().asFile.writeText(inputs)

        val summarizerStates = summarizersProperty.get().map { MetricSummarizerState(it) }
        sourceOutputDirectories.files.forEach { input ->
            summarizerStates.forEach { state ->
                // We should be receiving directories as inputs.  This is just a safeguard to ensure
                // that this is upheld.
                if (!input.isDirectory) {
                    throw GradleException("Unexpected non-directory input: $input")
                }

                // NOTE: The `ingestFile` API consumes a `RegularFile` Gradle API instance but we are starting
                // with a `File` instance.  This is a bit of a hack to convert the `File` to a `RegularFile`:
                val directory = objectFactory.directoryProperty().apply {
                    set(input)
                }.get()
                val inputFile = PathUtil.summarizerFile(directory, state.summarizer)

                state.ingestFile(inputFile.asFile)
            }
        }

        summarizerStates.forEach { state ->
            val outputFile = PathUtil.summarizerFile(outputDirectoryProperty.get(), state.summarizer)
            state.write(outputFile.asFile)
        }
    }
}