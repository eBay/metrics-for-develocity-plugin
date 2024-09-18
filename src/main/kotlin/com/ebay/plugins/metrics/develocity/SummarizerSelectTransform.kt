package com.ebay.plugins.metrics.develocity

import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault

/**
 * Artifact transform which extracts an individual summarizer's result from the output of the summary
 * computation task.
 */
@DisableCachingByDefault(because = "Copying files does not benefit from caching")
abstract class SummarizerSelectTransform : TransformAction<SummarizerSelectTransformParameters> {
    @get:InputArtifact
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = inputArtifact.get().asFile
        require(input.isDirectory) {
            "Input artifact must be a directory, but was: $input"
        }
        outputs.file(input.resolve(parameters.summarizerId.get()))
    }
}
