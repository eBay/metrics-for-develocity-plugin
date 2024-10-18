# Config Cache Miss Plugin


## Usage

This plugin defines a task name rule which can be used to generate a report of all
config cache miss reasons along with their frequency.  The task name specification
is of the form: `configCacheMissReport-<duration>`, where `<duration>` is a Java time duration
string ([JavaDocs](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Duration.html)).

## Query Filter

In order to specify the query criteria, the Develocity Metrics Plugin's query property is
used to provide a string in
[Develocity advanced search syntax](https://docs.gradle.com/enterprise/api-manual/#advanced_search_syntax).
For example: 

```shell
./gradlew '-PmetricsForDevelocityQueryFilter=tag:Local' configCacheMissReport-P7D
```

## Consolidating Similar Lines

Many of the reported cache miss reasons may look nearly identical, but have some variations
that are not important for reporting purposes.  These variations can be consolidated by
providing one or more regular expressions to the reporting task.

Each regular expression is applied to the cache miss reason, and if a match is found, the
behavior will vary based upon how the regular expression is defined:
- If the regular expression contains no capture groups, then the portion of the reason string
  that matches will be removed.  For example, if the regular expression is ` two ` and the
  reason is `one two three`, the resulting reason will be `onethree`.
- If the regular expression defines one or more capture groups, then the portion of the reason
  string that matches the regular expression will be replaced by a concatenation of all non-null
  capture groups.  For example, if the regular expression is ` (two) ` and the reason is
  `one two three`, the resulting reason will be `onetwothree`.

These rules can be used to simplify reason lines to make them identical, allowing them to be
bucketed together in the final report.

To specify a regular expression to be used in this manner, simply add the `--pattern <regex>`
argument after the task name.  For example:

```shell
./gradlew configCacheMissReport-P7D --pattern '(one) ' --pattern ' (three)'
```

By convention, some rules are applied if no patterns are provided. See the following code
for the default rules: [ConfigCacheMissReportTask.kt](ConfigCacheMissReportTask.kt#L22)

## Output

Upon completion, the report will be written to a file based on the task name which was run,
such as:
```shell
build/reports/configCacheMiss/configCacheMiss-P7D.txt
```
The report location will be printed to the console as task execution completes.