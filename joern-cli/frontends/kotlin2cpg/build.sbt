name := "kotlin2cpg"

val kotlinVersion    = "1.6.10"

dependsOn(Projects.dataflowengineoss)

libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "io.shiftleft"   %% "codepropertygraph"         % Versions.cpg,
  "io.shiftleft"   %% "semanticcpg"               % Versions.cpg,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.0" % Runtime,
  "org.slf4j"      % "slf4j-api"                  % "1.7.30",
  "org.scalatest"  %% "scalatest"                 % "3.1.4" % Test,
  "org.jetbrains.kotlin" % "kotlin-stdlib-jdk8"   % kotlinVersion,
  "org.jetbrains.kotlin" % "kotlin-compiler"      % kotlinVersion,
  "org.jetbrains.kotlin" % "kotlin-test"          % kotlinVersion % Test,
)

enablePlugins(JavaAppPackaging)
trapExit := false