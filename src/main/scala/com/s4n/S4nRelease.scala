package com.s4n

import sbt._
import Keys._
import sbtrelease._
import ReleaseStateTransformations.{ setReleaseVersion => _, _ }
import ReleasePlugin.autoImport._
import ReleaseTransformations._
import Utilities._
import sbt.Package.ManifestAttributes

import scala.language.postfixOps

object S4nRelease {

  import scala.util.Try

  val deployBranch = "master"
  val snapshotBranch = "develop"

  private def initialGitChecks( st: State ): Git = {
    val gitOpt = st.extract.get( releaseVcs ).flatMap( vcs => Try( vcs.asInstanceOf[Git] ).toOption )
    gitOpt.foreach {
      case gitRepo =>
        val hasModifiedFiles: Boolean = gitRepo.cmd( "ls-files", "--modified", "--exclude-standard" ).!!.trim.nonEmpty
        if ( hasModifiedFiles ) sys.error( "Aborting release: unstaged modified files" )
        st.log.info( "Starting release process off commit: " + gitRepo.currentHash )
    }
    if ( gitOpt.isEmpty ) sys.error( "Aborting release: not a git repository" )
    gitOpt.get
  }

  private def setVersionInFile( selectVersion: Versions => String ): ReleaseStep = { st: State =>
    val vs = st.get( ReleaseKeys.versions ).getOrElse( sys.error( "No versions are set! Was this release part executed before inquireVersions?" ) )
    val selected = selectVersion( vs )

    st.log.info( "Setting version to '%s'." format selected )
    val useGlobal = Project.extract( st ).get( releaseUseGlobalVersion )
    val versionStr = ( if ( useGlobal ) globalVersionString else versionString ) format selected
    val file = Project.extract( st ).get( releaseVersionFile )
    IO.writeLines( file, Seq( versionStr ) )

    reapply( Seq(
      if ( useGlobal ) version in ThisBuild := selected
      else version := selected
    ), st )
  }

  private def commitVersion( git: Git ): ( State ) => State = { st: State =>
    val file = st.extract.get( releaseVersionFile )
    val base = git.baseDir
    val relativePath = IO.relativize( base, file ).getOrElse( "Version file [%s] is outside of this VCS repository with base directory [%s]!" format ( file, base ) )

    git.add( relativePath ) !! st.log
    val status = ( git.status !! ) trim

    val newState = if ( status.nonEmpty ) {
      val ( state, msg ) = st.extract.runTask( releaseCommitMessage, st )
      git.commit( msg ) ! st.log
      state
    }
    else {
      // nothing to commit. this happens if the version.sbt file hasn't changed.
      st
    }
    newState
  }

  private def setAndCommitVersionToMasterAction: ReleaseStep = { st: State =>
    val git = initialGitChecks( st )
    if ( deployBranch != git.currentBranch ) git.cmd( "checkout", deployBranch ) ! st.log
    val newState = commitVersion( git )( setVersionInFile( _._1 )( st ) )
    reapply( Seq[Setting[_]](
      packageOptions += ManifestAttributes(
        "Vcs-Release-Hash" -> git.currentHash
      )
    ), newState )
  }

  private def setAndCommitVersionToDevelopAction: ReleaseStep = { st: State =>
    val git = initialGitChecks( st )
    if ( snapshotBranch != git.currentBranch ) git.cmd( "checkout", snapshotBranch ) ! st.log
    git.cmd( "merge", deployBranch ) ! st.log
    val newState = commitVersion( git )( setVersionInFile( _._2 )( st ) )
    git.cmd( "checkout", deployBranch ) ! st.log
    reapply( Seq[Setting[_]](
      packageOptions += ManifestAttributes(
        "Vcs-Release-Hash" -> git.currentHash
      )
    ), newState )
  }

  lazy val setAndCommitReleaseVersion = ReleaseStep( setAndCommitVersionToMasterAction )
  lazy val setAndCommitNextVersion = ReleaseStep( setAndCommitVersionToDevelopAction )

}

