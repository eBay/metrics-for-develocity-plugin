# Project Cost

This directory contains a summarizer implementation which is used to gather and report on project
module build costs.

## Usage

### Project Cost Inspection Report

In order to provide a bit more insight into project modules which are deemed to be "expensive", an
inspection report can be generated in order to examine the costs of a single module.

To generate the report, run the following command, replacing the project module to target,
as desired:

```
./gradlew :exampleModule:projectCostInspectionReport-P7D
```

Alternate durations may be specified by changing the `-P7D` suffix to the desired duration.
