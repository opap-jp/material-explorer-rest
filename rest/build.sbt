name := "material-explorer"
organization := "jp.opap"
version := "0.0.1"
scalaVersion := "2.12.4"
javacOptions ++= Seq("-encoding", "UTF-8")

val workaround: Unit = {
  sys.props += "packaging.type" -> "jar"
  ()
}
libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.12.4",
  "org.gitlab4j" % "gitlab4j-api" % "4.6.5" excludeAll(
    ExclusionRule(organization = "org.glassfish.jersey.inject"),
    ExclusionRule(organization = "org.glassfish.jersey.core"),
  ),
  "org.mongodb" % "mongo-java-driver" % "3.5.0",
  "io.dropwizard" % "dropwizard-core" % "1.2.0",
  "org.glassfish.jersey.media" % "jersey-media-sse" % "2.25.1",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.9.0.201710071750-r",
  "org.apache.httpcomponents" % "httpmime" % "4.5.3",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.3",
  "org.yaml" % "snakeyaml" % "1.19",
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "org.mockito" % "mockito-core" % "2.15.0" % "test"
)

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)
initialCommands := "import jp.opap.material._"

assemblyOutputPath in assembly := file(s"target/${name.value}.jar")
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
