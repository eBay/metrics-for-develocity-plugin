![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/com.ebay.metrics-for-develocity)

# Metrics for Develocity Plugin

## About This Project

The Metrics for Develocity Plugin is a Gradle plugin designed to query a
[Develocity](https://gradle.com/develocity/) server for build data, aggregating this data
in an extensible manner to allow for data gathering and reporting.

## Background

To better understand why this plugin exists and how it works, the following documents
may be referenced:
- [Motivation](docs/Motivation.md): Why the plugin was created and the problems it solves
- [Design Overview](docs/Design.md): High level overview of how the plugin functions

## Requirements

The plugin is designed to work with Gradle 8.8 or later.

## Usage

To enable the plugin, add the following to your project's `settings.gradle.kts` file:
```kotlin
plugins {
    id("com.ebay.metrics-for-develocity") version("<current version goes here>")
}
```

Then, configure the plugin in your root project's `build.gradle.kts` file:
```kotlin
extensions.configure<MetricsForDevelocityExtension> {
    develocity {
        // Optional: Set the timezone ID which should be used to define day boundaries.  Defaults
        // to the system's default timezone.
        zoneId.set("UTC")
        
        // Configure the base URL of the Develocity server to query.  By default, the server URL
        // configured for the `com.gradle.enterprise` or `com.gradle.develocity` plugin/extension
        // will be used.
        //
        // If no Develocity plugin is applied, the value may also be set by defining a
        // value for the `metricsForDevelocityServerUrl` gradle property.
        develocityServerUrl.set("https://custom-develocity-server.com")

        // Optional: Configure the access key to use when querying the Develocity server.
        // If an access key is explicitly provided to the `com.gradle.enterprise` or
        // `com.gradle.develocity` extension, that value will be used as a default.
        //
        // If no value is configured for this property then the Develocity key will be searched
        // for in the standard locations for manual key provisioning, documented here:
        // https://docs.gradle.com/develocity/gradle-plugin/current/#manual_access_key_configuration
        //
        // If no Develocity plugin is applied, the value may also be set by defining a
        // value for the `metricsForDevelocityAccessKey` gradle property.  If this approach
        // is used, take care to ensure that the access key is not captured in the build scan.
        develocityAccessKey.set("your_base64_encoded_access_key")
        
        // Optional: Configure the query filter to use when querying the Develocity server for
        // builds.  This filter is expressed using the Develocity's advanced search syntax:
        //  https://docs.gradle.com/enterprise/api-manual/#advanced_search_syntax
        // If no value is specified, the filter is set to filter for the current project
        //   "project:${rootProject.name}".
        develocityQueryFilter.set("tag:interesting-builds")
        
        // Optional: Set the maximum number of concurrent requests to make to the Develocity server
        // when querying build data.  Defaults to 24.
        develocityMaxConcurrency.set(10)
        
        // Optional: Add custom summarizers to the plugin.  These summarizers will be used to
        // build 
        summarizers.add(MyCustomSummarizer())
    }
}
```

For local development, the Develocity access token can be acquired by running the following command:
- `./gradlew provisionDevelocityAccessKey` or `./gradlew provisionGradleEnterpriseAccessKey`

To cause the builds to be queried and summarizers to run, Gradle task rules have been created
which use the following forms:
- `metricsForDevelocity-<datetime>` Examples:
  - `metricsForDevelocity-2024-06-01`: Queries all builds for 2024-06-01.
  - `metricsForDevelocity-2024-06-01T04`: Queries all builds which started 4AM <= X < 5AM of 2024-06-01.
- `metricsForDevelocity-last-<duration>` Examples:
  - `metricsForDevelocity-last-P7D`: Queries all builds for the last 7 days.
  - `metricsForDevelocity-last-PT8H`: Queries all builds for the last 8 hours.
  - `metricsForDevelocity-last-P2DT8H`: Queries all builds within the last 2 days and 8 hours.
  NOTE: When running queries which span multiple days, the plugin will automatically adjust the
  starting point to the beginning of the day if the start day is 7 days or more in the past.

## Provided Summarizers

This plugin ships with two example summarizer implementations which can be used as-is or
can be used as a reference for how to add custom summarizers.  Please refer to the following
documentation for more information on their purpose and usage:
- [Project Cost](src/main/kotlin/com/ebay/plugins/metrics/develocity/projectcost/README.md)
- [User Query](src/main/kotlin/com/ebay/plugins/metrics/develocity/userquery/README.md)

## Run Books

The following documents describe various processes needed for operating and/or maintaining
the plugin:
- [Run Book: Update the OpenAPI Specification](docs/RunBook-UpdatingOpenApiSpec.md)
- [Run Book: Release Process](docs/RunBook-ReleaseProcess.md)

## References

### Develocity API

API Manual: https://docs.gradle.com/enterprise/api-manual/
API Documentation: https://docs.gradle.com/enterprise/api-manual/ref/2022.4.html

## License

Apache 2.0 - See [LICENSE](LICENSE.txt) for more information.
