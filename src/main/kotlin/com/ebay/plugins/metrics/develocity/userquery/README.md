# User Query Plugin

This plugin leverages the Develocity Metrics Plugin, adding its own "summarizer" to gather the
usernames for all builds which match the specified criteria.

## Usage

This plugin defines a task name rule which can be used to generate a report of all
users who have performed builds matching specified criteria.  The task name specification
is of the form: `userQueryReport-<duration>`, where `<duration>` is a Java time duration
string ([JavaDocs](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Duration.html)).

In order to specify the query criteria, the Develocity Metrics Plugin's query property is
used to provide a string in
[Develocity advanced search syntax](https://docs.gradle.com/enterprise/api-manual/#advanced_search_syntax).
For example: 

```shell
./gradlew '-PmetricsForDevelocityQueryFilter=tag:Local' userQueryReport-P7D
```

Or an example using a more complex filter:
```shell
./gradlew gradle -PmetricsForDevelocityQueryFilter='(value:"CI build number=938") and (value:"CI job=gradle/gradle-profiler")' userQueryReport-P7D
```

Upon completion, the report will be written to a file based on the task name which was run,
such as:
```shell
build/reports/userQuery/userQuery-P7D.txt
```
The report location will be printed to the console as task execution completes.