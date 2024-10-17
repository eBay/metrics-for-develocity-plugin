package com.ebay.plugins.metrics.develocity

import com.gabrielfeo.develocity.api.model.Build
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/**
 * Helper which manages the state of a single summarizer.
 *
 * This helps us work around generic erasure issues by consolidating the type of all
 * calls made to a single summarizer into a single class.
 */
internal class MetricSummarizerState<T>(
    val summarizer: MetricSummarizer<T>,
) {
    private val stateRef = AtomicReference<T>()

    /**
     * Update the current state with the given intermediate data.
     */
    private tailrec fun update(intermediate: T) {
        val state = stateRef.get()
        val success = if (state == null) {
            stateRef.compareAndSet(null, intermediate)
        } else {
            val reduced = summarizer.reduce(state, intermediate)
            stateRef.compareAndSet(state, reduced)
        }
        if (success) return
        update(intermediate)
    }

    /**
     * Update the current state by adding a new build's data.  This will cause the summarizer
     * to extract the intermediate data from the build and then reduce it with the current state.
     */
    fun ingestBuild(build: Build) {
        val intermediate = summarizer.extract(build)
        update(intermediate)
    }

    /**
     * Update the current state by reading the intermediate data from the given file and then
     * reducing it with the current state.
     */
    fun ingestFile(file: File) {
        update(summarizer.read(file))
    }

    /**
     * Writes the current state to the file location specified.  If no data has been accumulated
     * then the file will not be written.
     */
    fun write(file: File) {
        stateRef.get()?.let {
            summarizer.write(it, file)
        }
    }

    /**
     * Provide a [String] representation which is a bit more useful for debugging.
     */
    override fun toString(): String {
        return "${javaClass.simpleName}(summarizer=${summarizer.id})"
    }
}