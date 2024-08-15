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

## Data consumer/reporting tasks

Data consumer / reporting tasks are configured to consume the aggregated data from the first
stage of the pipeline and produce data or reports in their final form.  These tasks are
to be implemented as needed to satisfy the requirements of the project.


