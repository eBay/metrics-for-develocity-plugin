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
