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

## Consumable `Configuration`s

In order to expose the root project's data to the consuming projects the root project defines
consumable `Configuration`s which export the data produced by the data gathering tasks to the
consuming projects.  These `Configuration`s use the same naming conventions as the data gathering
tasks.

### Pre-created `Configuration`s

In order for the consuming projects to be able to consume the data produced by the root
project's data gathering mechanisms from sub-projects, the root project needs to be instructed
as to what `Configuration`s to pre-create.  This is due to the fact that the desired
`Configuration` must exist at configuration time.

There are two mechanisms built to satisfy this requirement:
- When the plugin is applied as a settings plugin, it will look at the requested task list for any
  task name which is suffixed with `-<timeSpec>`, where `<timeSpec>` may either be a datetime
  specification (e.g. `2024-10-21`) or a duration specification (e.g. `P7D`).  The plugin will
  then automatically pre-create the `Configuration` for the task with no additional effort required.
- If the task name detection approach does not satisfy the underlying requirement of the consuming
  project (e.g., if the task name does not contain a time specification) then the
  `metricsForDevelocityConfigurations` gradle property may be supplied with a comma-delimited
  list of time specifications to automatically pre-create the configurations for.  This is a
  bit hacky, but works in lie of a Gradle API being created to allow for dynamic configuration
  creation (xref: https://github.com/gradle/gradle/issues/30831).

### Basic task wiring

To simplify the chore of wiring up the consuming projects, the
[TaskProviderExtensions](../src/main/kotlin/com/ebay/plugins/metrics/develocity/TaskProviderExtensions.kt)
file provides a set of extension functions that can be used to wire up an individual
summarizer's output of the data gathering task to a consuming project's
`TaskProvider<out MetricSummarizerTask>` instance.

### Advanced task wiring

For more advanced use cases where the provided extensions are inadequate, the consuming
project can create a resolvable Gradle `Configuration` to refer to the root project's
`Configuration` by name and attribute configuration.  When this approach is used, the resolvable
`Configuration` should specify the following attributes:
- [SUMMARIZER_ATTRIBUTE](../src/main/kotlin/com/ebay/plugins/metrics/develocity/MetricsForDevelocityConstants.kt)
  with a value of [SUMMARIZER_ALL], which will result in a directory of all summarizer outputs.
- [TIME_SPEC_ATTRIBUTE](../src/main/kotlin/com/ebay/plugins/metrics/develocity/MetricsForDevelocityConstants.kt)
  with a value of the datetime or duration specification (e.g., `2024-10-21` or `P7D`)

At this point, the summarizer output file would ideally be selected via an artifact transform
provided by this plugin.  Unfortunately, the Gradle API for this is not quite workable at this
time.  For now, simply use the output directory provided and resolve the file named with the
summarizer's ID, directly.  See the
[TaskProviderExtensions](../src/main/kotlin/com/ebay/plugins/metrics/develocity/TaskProviderExtensions.kt)
helper method implementations for an example of how this is done.

## Data consumer / reporting tasks

Data consumer / reporting tasks are configured to consume the aggregated data from the first
stage of the pipeline and produce data or reports in their final form.  These tasks are
to be implemented as needed to satisfy the requirements of the project.
