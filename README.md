# S4N sbt release process



![S4N Logo](http://s4n.co/images/s4n_logo.png)

### Uso:

  Agregar resolver y credenciales en archivo ./project/s4n-release.sbt
```scala
  resolvers ++= Seq(
    "S4N Releases"  at "http://nexus.seven4n.com/content/repositories/s4n-base-releases"
  )
  
  credentials += Credentials(
    "Sonatype Nexus Repository Manager", 
    "nexus.seven4n.com", 
    "s4nbase", 
    "s4nbase"
  )
```

  Agregar al archivo ./project/plugin.sbt:
```scala
  addSbtPlugin("com.s4n" % "sbt-s4n-release" % "0.1.2")
```

### Proceso de Release:

  El proceso de release se configura con el SettingKey releaseProcess, la configuracion por defecto es la siguiente
```scala
  releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        setAndCommitReleaseVersion,
        tagRelease,
        setAndCommitNextVersion,
        pushChanges
      )
```

### Recursos

[SBT-Release](http://github.com/sbt/sbt-release)

[SBT-BuildInfo](http://github.com/sbt/sbt-buildinfo)

[SBT-GIT](http://github.com/sbt/sbt-git)

[SBT-Scalariform](https://github.com/sbt/sbt-scalariform)
