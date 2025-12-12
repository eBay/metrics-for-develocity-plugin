# Task Duration Plugin

This plugin adds a summarizer and a report task that aggregates the total execution duration
of a specific task, identified by either task name (across all modules) or by task type.

## Usage

### By Task Name (Default)

The plugin provides a task rule that creates report tasks with the following pattern:

```
taskDurationReport-<Task Name>-<Java Duration String>
```

For example:
- `taskDurationReport-compileJava-P7D` - Reports on all `compileJava` tasks across all modules
  for the last 7 days
- `taskDurationReport-test-PT8H` - Reports on all `test` tasks across all modules for the
  last 8 hours
- `taskDurationReport-assemble-P2DT8H` - Reports on all `assemble` tasks across all modules for
  the last 2 days and 8 hours

### By Task Type

You can also report on tasks by type using the following task name pattern, providing the
type of the task as a task argument:

```
taskTypeDurationReport-<Java Duration String>
```

For example:
- `taskTypeDurationReport-P7D --task-type com.osacky.flank.gradle.FlankExecutionTask` - Reports
  on all task invocations that had the `com.osacky.flank.gradle.FlankExecutionTask` type.
- `taskTypeDurationReport-P7D --task-type org.jetbrains.kotlin.gradle.tasks.KotlinCompile` - Reports
  on all invocations of the Kotlin compilation task.  This would - for example - include all
  invocations of `compileKotlin` as well as `compileDebugKotlin` on Android projects.

## Duration String Specification

The duration string used in the task names follows the
[Java Duration format](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-)
standard.

## Filtering

This report may also be combined with the plugin's `develocityQueryFilter` specification
to only include tasks run witin builds that match the filter specification.

For example:
- `./gradlew -PmetricsForDevelocityQueryFilter=tag:ci-premerge taskDurationReport-test-PT8H`
- `./gradlew -PmetricsForDevelocityQueryFilter='(value:"CI build number=938") and (tag:ci-premerge)' taskDurationReport-test-P7D`

## Report Output

The report output is generated in the `build/reports/taskDuration/` directory and would look
similar to the following:
```text
Task Duration Report for task type 'com.osacky.flank.gradle.FlankExecutionTask'
===============================================================================

Report Generated At: 2025-12-12T10 (America/Los_Angeles)
Total Execution Duration: 2d9h36m
Number of Task Executions: 367
Average Execution Duration: 9m25s

Daily Breakdown
===============

Date         Count      Total        Min        Avg        Max
---------- ------- ---------- ---------- ---------- ----------
2025-12-10     113  19h28m52s      3m15s     10m20s     29m43s
2025-12-11     190    1d5h46m      2m55s      9m24s     24m16s
2025-12-12      64   8h20m44s      3m17s      7m49s     21m56s
```
