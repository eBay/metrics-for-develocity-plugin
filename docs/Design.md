# Design Overview

The data processing pipeline is split into two main elements:
- Data gathering and aggregation
- Data consumption / reporting

## Data gathering tasks:

The purpose of the data gathering tasks is to collect data from the Develocity API and
aggregate it into summaries that can be used for reporting.  The data for this stage must be
defined such that it is purely additive and can be accumulated over time through a reduction
operation, adding the  results of one build to the results of the next, etc..
The data collected in this stage can be thought of as an intermediate format, not intended for
direct consumption.

Multiple summaries can be created as a result of this stage of processing.  Summarizers are
registered with the plugin and have the responsibility of processing the raw build data
and producing the intermediate data format.

Hourly tasks
- Query the Develocity server for list of builds within the hour
- For each configured summarizer:
    - Process each build and produce a summary file
    - reduces the summaries together, persisting them to disk by ID

Daily tasks
- Depends upon the outputs of hourly tasks for the configured day
- For each configured summarizer:
    - Loads the hourly summary files
    - reduces the summaries together, persisting them to disk by ID

Time window tasks (e.g., last 7 days)
- Depends upon the outputs of hourly and/or daily tasks to satisfy the requested time window
- For each configured summarizer:
    - Loads the hourly summary files
    - reduces the summaries together, persisting them to disk by ID

All data gathering tasks are performed only in the root project.

## Consumable configurations

In order to expose the root project's data to the consuming projects the root project defines
consumable configurations which export the data produced by the data gathering tasks to the
consuming projects.  These configurations use the same naming conventions as the data gathering
tasks.

To simplify the chore of wiring up the consuming projects, the
[TaskProviderExtensions](../src/main/kotlin/com/ebay/plugins/metrics/develocity/TaskProviderExtensions.kt)
file provides a set of extension functions that can be used to wire up an individual sumarizer's output of the data
gathering task to a consuming project's `TaskProvider<out MetricSummarizerTask>` instance.

For more advanced use cases, the consuming project can create a resolvable Gradle `Configuration` to refer to
the root project's configuration by name. When this approach is used, the resolvable configuration should specify the
[SUMMARIZER_ATTRIBUTE](../src/main/kotlin/com/ebay/plugins/metrics/develocity/MetricsForDevelocityConstants.kt)
with a value of [SUMMARIZER_ALL], which will result in a directory of all summarizer outputs.  To select an
individual summarizer's output, the consuming project can leverage the
[SummarizerSelectTransform](../src/main/kotlin/com/ebay/plugins/metrics/develocity/SummarizerSelectTransform.kt)
artifact transform to get at the desired individual output file.  

## Data consumer / reporting tasks

Data consumer / reporting tasks are configured to consume the aggregated data from the first
stage of the pipeline and produce data or reports in their final form.  These tasks are
to be implemented as needed to satisfy the requirements of the project.
