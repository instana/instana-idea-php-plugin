# Instana Support for IntelliJ based IDEs

A plugin for IntelliJ providing convenient shortcuts when working with the Instana PHP SDK.

## Features

### PHP

* suggest using the Instana PHP SDK when usages are found
* allow manual tracing of method calls

## Development

Import `build.gradle` as a new project.

`./gradlew runIde` will run a sample IDE for poking around

## Releasing

`./prepare-release.sh` will build the change-notes, commit.

`./gradlew buildPlugin` will build the artifact

`IJ_REPO_TOKEN=my-token ./gradlew publishPlugin` will publish the new
version on the JetBrains marketplace.

## License

MIT
