libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / "src" / "main" / "scala" / "com" / "s4n"