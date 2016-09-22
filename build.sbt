sbtPlugin := true

//Change to your organization
organization := "com.s4n"

//Change to your plugin name
name := """sbt-s4n-release"""

//Change to the version
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("snapshots")

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"


// Scripted - sbt plugin tests
scriptedSettings

scriptedLaunchOpts += "-Dproject.version=" + version.value


fork in run := true
