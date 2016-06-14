# xploration-testing
The idea behind this repo is to easily collect all the groups code via a Maven repo and then run tests on them.


## Running
To run the tests on Linux or macOS, Java 8 is needed.

The platform and companies are started via Gradle:

    ./gradlew runN

Where N is the platform you want to run, e. g. To run platform 2 from company 2:

    ./gradlew run2
