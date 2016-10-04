![S4N Logo](http://s4n.co/images/s4n_logo.png)

# S4N sbt release process

  S4nRelease es un plugin de sbt que integra varias herramientas para
  facilitar el proceso de release de los proyectos. Cuenta con 
  [SBT-Release](http://github.com/sbt/sbt-release) para definir pasos base
  de release y poder agregar pasos personalizados. 
  
  Adicionalmente se incluyen los plugins:
  -[SBT-BuildInfo](http://github.com/sbt/sbt-buildinfo) y [SBT-GIT](http://github.com/sbt/sbt-git) para poder agregar información del versionamiento y commit de la aplicación.
  -[SBT-Scalariform](https://github.com/sbt/sbt-scalariform) para formatear el código usando [Scalariform](https://github.com/scala-ide/scalariform).  

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

  Para realizar el release con sbt es necesario hacer tracking de la
  rama **master** en el repositorio remoto:
  
```
  git branch -u origin/master
``` 

  Si no existe la rama master en tu local por alguna razón:
```
  git branch -u origin/master master
```

  Se requiere crear el archivo ./version.sbt en la rama develop e 
  incluir:
```
  version in ThisBuild := "0.1.0-SNAPSHOT"
```  
  
  Una vez integrados todos los commits de los features a develop, se 
  deben seguir los siguientes pasos en el root de tu proyecto:
```
  git checkout master
  git merge develop
  sbt release
```  

  Se necesitará introducir la nueva version y la version SNAPSHOT en la
  linea de comandos. Finalmente se realizará el push al master remoto.
  Se recomienda hacer checkout a develop y subir los cambios inmediatemente
  después de realizar el release.

  El proceso de release se configura con el SettingKey releaseProcess, 
  la configuracion por defecto es la siguiente
```scala
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setAndCommitReleaseVersion, //Paso personalizado S4nRelease
    tagRelease,
    setAndCommitNextVersion, //Paso personalizado S4nRelease
    pushChanges
  )
```
