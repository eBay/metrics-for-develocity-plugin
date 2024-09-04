# Project Cost

This directory contains a summarizer implementation which is used to gather and report on project
module build costs.

## Usage

### Project Cost Inspection Report

In order to provide a bit more insight into project modules which are deemed to be "expensive", an
inspection report can be generated in order to examine the costs of a single module.  This report
will include the following information:
- Total number of builds which included the project module
- Total number of builds in which at least one task was executed for the project module
- Total number of unique user names which executed a build with the project module
- Total aggregated (serial) build time for all tasks executed within the project module
- Top 25 tasks by average duration (by task name and by type)
- Top 25 tasks by execution count (by task name and by type)
- A list of all unique user names who performed builds including the project module
- A list of build scan URLs for all builds which ran tasks within the project module

To generate the report, run the following command, replacing the project module to target,
as desired:

```
./gradlew :exampleModule:projectCostInspectionReport-P7D
```

Alternate durations may be specified by changing the `-P7D` suffix to the desired duration.

### Project Cost Graph Analytics Integration

The project cost summarizer data can be easily integrated into the data collection and reporting
performed by the [eBay Graph Analytics Plugin](https://github.com/eBay/graph-analytics-plugin).
This integration allows for the resulting project graph data to include the execution-time
costs for each project module, granting a more comprehensive picture of the project's build.

To enable the integration, the gradle property `projectCostGraphAnalysisEnabled` must be
set to `true`.  Since the data gathering phase for the project cost summarizer can be quite
expensive to gather, it is recommended to run this integration in a dedicated job.

Additionally, since the project cost reporting aggregates data over a period of time, the
duration for the report must be specified.  This is done by setting the
`projectCostGraphAnalysisDuration` property to a Java time duration string.  If not specified,
data form the last 7 days will be used.

Putting it all together, the following command can be used to generate a graph analysis
incorporating the project cost data over a custom time range of 3 days:

```
./gradlew -PprojectCostGraphAnalysisEnabled=true -PprojectCostGraphAnalysisDuration=P2D graphAnalysis 
```

The resulting graph analysis will include the following project cost data for each project module:

| Metric ID            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                   |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `buildAvgDuration`   | Total duration of all executed tasks within the project module divided by the number of builds in which at least one task was executed.                                                                                                                                                                                                                                                                                                       |
| `buildAvgTasks`      | The average number of tasks executed within the project module per build in which at least one task was executed.                                                                                                                                                                                                                                                                                                                             |
| `buildCount`         | The number of builds in which at least one task was executed within the project module.  A task is considered executed if it did work (e.g. was not up-to-date, pulled from cache, etc.).                                                                                                                                                                                                                                                     |
| `buildCostScalar`    | A generate composite value which represents the cost of the project module, meant to be used to perform relative comparisons between project modules.  While the algorithm for calculating this may change over time, it is currently calculated as the `buildAvgDuration` multiplied by the `buildPercentage`, truncated to an integer value.  This is meant to surface modules which are built frequently that are also expensive to build. |
| `buildDuration`      | The total duration of all tasks within the project module.  This includes the costs of tasks which were up-to-date, pulled from cache, etc.                                                                                                                                                                                                                                                                                                   |
| `buildPercentage`    | The percentage of builds in which at least one task was executed within the project module.                                                                                                                                                                                                                                                                                                                                                   |
| `buildImpactedUsers` | The total number of unique user IDs who have been impacted by the build costs of the project module.                                                                                                                                                                                                                                                                                                                                          |

