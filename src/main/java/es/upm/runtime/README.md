#Xploration Runtime
The `XplorationRuntime` initializes a JADE runtime, creates and starts agents
found at their standardized locations in the class path.

The companies can be loaded either by being added as a dependency and
rebuilding the JAR or by dropping library files into the `libs` folder.

###Usage
Add your company as a dependency into the `build.gradle`.
Compile the JAR with the `fatJar` task. This will pack all the
dependencies into it too.

Companies can also be loaded by being dropped into a libs folder in
the same directory as the JAR. The runtime will expect them to have name in the
following format `company0X.jar`.

**Note**: We could load all the libraries at runtime and init the agents from there.
But since ATOW are using JADE's `[code=xxx.jar]` functionality, it's this way.

###Arguments
Right now the arguments are just passed blatantly.

1. Libs path. Any path will do just fine.
2. Spacecraft number. If none is given, uses the one from Team03 (Borys&Co).
