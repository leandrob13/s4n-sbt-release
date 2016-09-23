package com.s4n

import sbt._
import Keys._
import com.typesafe.sbt.SbtGit.git
import sbtrelease._
import sbtbuildinfo._

/**
 * This plugin helps you which operating systems are awesome
 */
object S4nReleasePlugin extends AutoPlugin {
  import ReleasePlugin.autoImport._
  import scalariform.formatter.preferences._
  import com.typesafe.sbt.SbtScalariform._

  /**
   * Defines all settings/tasks that get automatically imported,
   * when the plugin is enabled
   */
  object autoImport extends BuildInfoKeys {
    val BuildInfoKey = sbtbuildinfo.BuildInfoKey
    type BuildInfoKey = sbtbuildinfo.BuildInfoKey
    val BuildInfoOption = sbtbuildinfo.BuildInfoOption
    type BuildInfoOption = sbtbuildinfo.BuildInfoOption
    val BuildInfoType = sbtbuildinfo.BuildInfoType
    type BuildInfoType = sbtbuildinfo.BuildInfoType
    val addBuildInfoToConfig = BuildInfoPlugin.buildInfoScopedSettings _
  }

  import autoImport._
  import S4nRelease._
  import ReleaseKeys._
  import ReleaseTransformations._

  /**
   * Provide default settings
   */

  val VersionRegex = "v([0-9]+.[0-9]+.[0-9]+)-?(.*)?".r

  override def trigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = BuildInfoPlugin.buildInfoScopedSettings( Compile ) ++ Seq(
    buildInfoObject := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoUsePackageAsPath := false,
    buildInfoBuildNumber := BuildInfoPlugin.buildNumberTask( baseDirectory.value, 1 ),
    buildInfoKeys := Seq[BuildInfoKey]( name, version, scalaVersion, sbtVersion ),
    buildInfoOptions := Seq( BuildInfoOption.ToJson, BuildInfoOption.BuildTime ),
    buildInfoKeys += BuildInfoKey.action( "revision" ) {
      git.gitHeadCommit.value.getOrElse( "Could not evaluate" )
    },
    git.useGitDescribe := true,
    git.baseVersion := version.value,
    git.gitTagToVersionNumber := {
      case VersionRegex( v, "" )         => Some( v )
      case VersionRegex( v, "SNAPSHOT" ) => Some( s"$v-SNAPSHOT" )
      case VersionRegex( v, s )          => Some( s"$v-$s-SNAPSHOT" )
      case _                             => None
    },
    releaseSnapshotDependencies := {
      val moduleIds = ( managedClasspath in Runtime ).value.flatMap( _.get( moduleID.key ) )
      val snapshots = moduleIds.filter( m => m.isChanging || m.revision.endsWith( "-SNAPSHOT" ) )
      snapshots
    },
    releaseVersion := { ver => Version( ver ).map( _.withoutQualifier.string ).getOrElse( versionFormatError ) },
    releaseVersionBump := Version.Bump.default,
    releaseNextVersion := {
      ver => Version( ver ).map( _.bump( releaseVersionBump.value ).asSnapshot.string ).getOrElse( versionFormatError )
    },
    releaseUseGlobalVersion := true,
    releaseCrossBuild := false,
    releaseTagName := s"v${if ( releaseUseGlobalVersion.value ) ( version in ThisBuild ).value else version.value}",
    releaseTagComment := s"Releasing ${if ( releaseUseGlobalVersion.value ) ( version in ThisBuild ).value else version.value}",
    releaseCommitMessage := s"Setting version to ${if ( releaseUseGlobalVersion.value ) ( version in ThisBuild ).value else version.value}",
    releaseVcs := Vcs.detect( baseDirectory.value ),
    //releaseVcsSign := false,
    releaseVersionFile := baseDirectory.value / "version.sbt",
    releasePublishArtifactsAction := publish.value,
    releaseIgnoreUntrackedFiles := false,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setAndCommitReleaseVersion,
      tagRelease,
      setAndCommitNextVersion,
      pushChanges
    ),
    commands += releaseCommand,

    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference( AlignSingleLineCaseStatements, true )
      .setPreference( DoubleIndentClassDeclaration, true )
      .setPreference( DanglingCloseParenthesis, Force )
      .setPreference( AlignParameters, true )
      .setPreference( CompactControlReadability, true )
      //.setPreference(SpaceInsideBrackets, false)
      .setPreference( SpaceInsideParentheses, true )
      .setPreference( SpacesWithinPatternBinders, true )
  )

}