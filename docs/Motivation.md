# Motivation

After building the [Graph Analytics Plugin](https://github.com/eBay/graph-analytics-plugin)
we analyzed our project and found that we wanted to incorporate the cost of building
project modules into our project graph analysis.  In order to do this, we needed to
query our Develocity server to gather and calculate these metrics, then integrate them
into our project graph data to provide a holistic view of our project.

Additionally, on occasion there was a need to query the Develocity data to investigate
specific build scenarios or to create a report on builds that matched a particular criteria.
Once this plugin was introduced into our project, we found that we were able to easily
solve for these use cases as well.

The Metrics for Develocity Plugin was born out of the need to address these requirements.
