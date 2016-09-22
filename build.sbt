sbtPlugin := true

//Change to your organization
organization := "com.s4n"

//Change to your plugin name
name := """sbt-s4n-release"""

//Change to the version
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.4"
//scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-feature")

/*lazy val root = (project in file(".")).enablePlugins(ReleasePlugin, BuildInfoPlugin, GitVersioning,
  GitBranchPrompt, SbtScalariform)*/

resolvers ++= Seq(
  "releases" at "http://oss.sonatype.org/content/repositories/releases",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "sbt-bintray" at "http://dl.bintray.com/sbt/sbt-plugin-releases",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases",
  "MVN Repository" at "http://central.maven.org/maven2",
  Resolver.sonatypeRepo("releases")
)

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"


addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// Scripted - sbt plugin tests
scriptedSettings

scriptedLaunchOpts += "-Dproject.version=" + version.value


fork in run := true

credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.seven4n.com", "sura_cotizadores", "sura_cotizadores")

publishTo := {
  val nexus = "http://nexus.seven4n.com/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("S4N Nexus Snapshots" at nexus + "content/repositories/sura_cotizadores_snapshot/")
  else
    Some("S4N Nexus Releases" at nexus + "content/repositories/sura_cotizadores_release/")
}
