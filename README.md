![S4N Logo](http://s4n.co/images/s4n_logo.png)

# S4N sbt release process

  S4nRelease es un plugin de sbt que integra varias herramientas para facilitar el proceso de release de los 
  proyectos. Cuenta con [SBT-Release](http://github.com/sbt/sbt-release) para definir pasos base y poder agregar pasos personalizados. 
  
  Adicionalmente se incluyen los plugins:
  
  * [SBT-BuildInfo](http://github.com/sbt/sbt-buildinfo) y [SBT-GIT](http://github.com/sbt/sbt-git) para poder agregar información del versionamiento y commit de las aplicaciones.
  * [SBT-Scalariform](https://github.com/sbt/sbt-scalariform) para formatear el código usando [Scalariform](https://github.com/scala-ide/scalariform).  

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
  addSbtPlugin("com.s4n" % "s4n-sbt-release" % "0.1.4")
```

  Y por último se agrega en el build.sbt:
```scala
  val root = (project in file(".")).enablePlugins(S4nReleasePlugin)
```

### Proceso de Release:

  Para realizar el release con sbt es necesario hacer tracking del master local a
  rama **master** en el repositorio remoto:
    
```
  git checkout master
  git branch -u origin/master
``` 

  Si por alguna razón no existe la rama master en el repo local:
```
  git branch -u origin/master master
```

  Se requiere crear el archivo ./version.sbt en la rama develop e 
  incluir en él:
```
  version in ThisBuild := "0.1.0-SNAPSHOT"
```  
  
  Una vez integrados todos los commits de los features a develop, se 
  deben seguir los siguientes pasos en el root del proyecto:
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

### BuildInfo y Git:

  BuildInfo nos permite generar en tiempo de compilación un object que contiene información útil
  del proyecto y combinado el sbt-git se puede extraer incluso el hash del commit que se utilizó 
  para generar el build. La configuración por defecto en S4nRelease es:
  
```scala
  buildInfoObject := "BuildInfo"
  buildInfoPackage := "com.s4n.build"
  buildInfoUsePackageAsPath := false
  buildInfoBuildNumber := BuildInfoPlugin.buildNumberTask( baseDirectory.value, 1 )
  buildInfoKeys := Seq[BuildInfoKey]( name, version, scalaVersion, sbtVersion )
  buildInfoOptions := Seq( BuildInfoOption.ToJson, BuildInfoOption.BuildTime )
  buildInfoKeys += BuildInfoKey.action( "revision" ) {
    git.gitHeadCommit.value.getOrElse( "Could not evaluate" )
  }
  git.useGitDescribe := true
  git.baseVersion := version.value
  git.gitTagToVersionNumber := {
    case VersionRegex( v, "" )         => Some( v )
    case VersionRegex( v, "SNAPSHOT" ) => Some( s"$v-SNAPSHOT" )
    case VersionRegex( v, s )          => Some( s"$v-$s-SNAPSHOT" )
    case _                             => None
  }
```  

  Al compilar genera el siguiente código:

```scala
package com.s4n.build

import scala.Predef._

/** This object was generated by sbt-buildinfo. */
case object BuildInfo {
  /** The value is "s4n-sbt-release". */
  val name: String = "sbt-s4n-release"
  /** The value is "0.1.2". */
  val version: String = "0.1.2"
  /** The value is "2.10.4". */
  val scalaVersion: String = "2.10.4"
  /** The value is "0.13.8". */
  val sbtVersion: String = "0.13.8"
  /** The value is "9e5195cf0ddef9bc72c3ba00ab722df796c7113d". */
  val revision: String = "9e5195cf0ddef9bc72c3ba00ab722df796c7113d"
  /** The value is "2016-09-23 22:02:56.398". */
  val builtAtString: String = "2016-09-23 22:02:56.398"
  /** The value is 1474668176398L. */
  val builtAtMillis: scala.Long = 1474668176398L
  override val toString: String = {
    "name: %s, version: %s, scalaVersion: %s, sbtVersion: %s, revision: %s, builtAtString: %s, builtAtMillis: %s" format (
      name, version, scalaVersion, sbtVersion, revision, builtAtString, builtAtMillis
    )
  }
  val toMap: Map[String, Any] = Map[String, Any](
    "name" -> name,
    "version" -> version,
    "scalaVersion" -> scalaVersion,
    "sbtVersion" -> sbtVersion,
    "revision" -> revision,
    "builtAtString" -> builtAtString,
    "builtAtMillis" -> builtAtMillis
  )

  val toJson: String = toMap.map( i => "\"" + i._1 + "\":\"" + i._2 + "\"" ).mkString( "{", ", ", "}" )
}

```

  Y se puede usar en un controlador de Play para exponer un servicio de salud de la aplicación:
  
```scala
  package controllers
   
  import play.api.mvc._
  import com.s4n.build.BuildInfo
   
  class Application extends Controller {
   
    def index = Action {
      Ok( BuildInfo.toJson ) as JSON
    }
  
  }
```
