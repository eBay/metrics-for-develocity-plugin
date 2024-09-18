package com.ebay.plugins.metrics.develocity

import com.ebay.plugins.metrics.develocity.service.model.Build
import com.ebay.plugins.metrics.develocity.service.model.BuildModelName
import java.io.File

/**
 * Build data summarizer which can be used to process build data into an intermediate form,
 * allowing the data to be aggregated/reduced over windows of time.
 */
abstract class MetricSummarizer<Intermediate> {
    /**
     * A unique identifier for the summarizer.  This will also become the filename for the intermediate
     * data file within the output directory of the metric gathering and aggregation tasks.
     */
    open val id: String = javaClass.simpleName

    /**
     * The set of build model types which the summarizer needs to process in order to produce its
     * intermediate data.
     */
    open val modelsNeeded: Set<BuildModelName> = emptySet()

    /**
     * Reads intermediate data from a file.  If the file provided does not exist then the
     * method should return an empty intermediate data object.
     */
    abstract fun read(file: File): Intermediate

    /**
     * Writes an intermediate data object to the file specified.
     */
    abstract fun write(intermediate: Intermediate, file: File)

    /**
     * Processes a single build and produces an intermediate representation.
     */
    abstract fun extract(build: Build): Intermediate

    /**
     * Reduces two intermediate data objects into a single intermediate data object.
     */
    abstract fun reduce(left: Intermediate, right: Intermediate): Intermediate
}
