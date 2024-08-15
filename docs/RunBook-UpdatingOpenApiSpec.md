# Run Book: Updating the OpenAPI Specification

## Process

1. Download the current specification.  This is published in the Develocity documentation and 
   can be found [here](https://docs.gradle.com/enterprise/api-manual/#reference_documentation).
2. Place this new specification file in the `src/openapi` directory.
3. Run the following: `./gradlew openApiGenerate`
4. Delete the existing `src/main/kotlin/com/ebay/plugins/metrics/develocity/service/model`
   source files (but leave the README).
5. Copy the generated source files:
   ```
   mv build/generated/openApi/src/main/kotlin/com/ebay/plugins/metrics/develocity/service/model/* \
   src/main/kotlin/com/ebay/plugins/metrics/develocity/service/model/
   ```
6. Test and commit the results

### OpenAPI

This project uses the OpenAPI 3.0 specification, using the
[OpenAPI generator](https://github.com/OpenAPITools/openapi-generator)
([OpenAPI Gradle Plugin](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin)
specifically) to output models.



