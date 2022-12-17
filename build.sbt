val scala3version = "3.1.2"
val pluginVersion = "0.1.0-SNAPSHOT"
val org = "net.robmwalsh"

addCommandAlias(
  "demo",
  ";root/clean;research/publishLocal;reload;demo/run"
)

lazy val research = project
  .settings(
    name := "research",
    organization := org,
    version := pluginVersion,
    scalaVersion := scala3version,
    libraryDependencies += "org.scala-lang" %% "scala3-compiler" % scala3version % "provided"
  )

lazy val demo = project
  .settings(
    name := "demo",
    version := pluginVersion,
    publish / skip := true,
    scalaVersion := scala3version,
    libraryDependencies +=
      compilerPlugin(org %% "research" % pluginVersion)
  )

lazy val root = project
  .aggregate(research, demo)
  .settings(
    publish / skip := true
  )
