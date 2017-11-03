name := "material-explorer"
organization := "jp.opap"
version := "0.0.1"
scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "io.dropwizard" % "dropwizard-core" % "1.2.0",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.9.1",
)

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)
initialCommands := "import jp.opap.material._"

assemblyOutputPath in assembly := file(s"target/${name.value}.jar")
