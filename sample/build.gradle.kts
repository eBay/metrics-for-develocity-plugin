metricsForDevelocity {
    // The following could be used to change the time zone used by the plugin:
    // zoneId.set("UTC")

    // An additional filter may also be supplied:
    // develocityQueryFilter.set("project:andr_core tag:Local")

    // This sample project will hit the community cloud Develocity server so we tightly
    // restrict concurrency, to be good citizens.
    develocityMaxConcurrency.set(2)
}