

//lazy val `sbt-s4n-release` = project in file(".")
lazy val `sbt-s4n-release` = (project in file(".")).enablePlugins(S4nReleasePlugin)
import sbt._
//import S4nReleasePlugin._

sbtPlugin := true

//Change to your organization
organization := "com.s4n"

//Change to your plugin name
name := """s4n-sbt-release"""

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
  "MVN Repository" at "http://central.maven.org/maven2"
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

credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.seven4n.com", "s4nbase", "s4nbase")
//http://nexus.seven4n.com/content/repositories/s4n-base-releases/
publishTo := {
  val nexus = "http://nexus.seven4n.com/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("S4N Nexus Snapshots" at nexus + "content/repositories/s4n-base-snapshot/")
  else
    Some("S4N Nexus Releases" at nexus + "content/repositories/s4n-base-releases/")
}
