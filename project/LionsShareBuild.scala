import sbt._
import Keys._
import Package.ManifestAttributes
import sbtassembly.AssemblyPlugin._
import sbtassembly.AssemblyKeys.assembly
import com.typesafe.sbt.SbtScalariform._
import scoverage.ScoverageSbtPlugin._
//import sbtrelease.ReleasePlugin._
//import ReleaseKeys._
//import com.typesafe.sbt.pgp.PgpKeys
//import PgpKeys._

object LionBuild extends Build {
  def module(dir: String, settings: Seq[Setting[_]] = commonSettings) =
    Project(id = dir, base = file(dir), settings = settings)

  lazy val agent = module("agent", settings = commonSettings ++ Seq(
    // disables scala for pure Java module
    autoScalaLibrary := false, crossPaths := false,
    libraryDependencies ++= Seq(
      "org.projectlombok" % "lombok" % "1.16.2" % "provided",
      "com.github.fommil" % "java-allocation-instrumenter" % "3.0"
    ),
    packageOptions := Seq(ManifestAttributes(
      "Premain-Class" -> "com.github.fommil.lion.agent.AllocationAgent",
      "Boot-Class-Path" -> "agent-assembly.jar",
      "Can-Redefine-Classes" -> "true",
      "Can-Retransform-Classes" -> "true",
      "Main-Class" -> "NotSuitableAsMain"
    )),
    artifact in (Compile, assembly) ~= { art =>
      art.copy(`classifier` = Some("assembly"))
    }
  ) ++ addArtifact(artifact in (Compile, assembly), assembly).settings)

  lazy val analysis = module("analysis") settings (
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "18.0",
      "com.google.code.findbugs" % "jsr305" % "2.0.3",
      "io.spray" %% "spray-json" % "1.3.1",
      "org.parboiled" %% "parboiled-scala" % "1.1.7",
      "com.typesafe.akka" %% "akka-slf4j" % "2.3.9" excludeAll (bad: _*),
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "org.apache.commons" % "commons-math3" % "3.4.1",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  )

  lazy val sbt = module("sbt") dependsOn (analysis) settings (
    sbtPlugin := true
  )

  // would be nice not to have to define the 'root'
  lazy val root = Project(id = "parent", base = file("."), settings = commonSettings) aggregate (
    agent, analysis, sbt
  ) dependsOn (sbt)

  lazy val commonSettings = scalariformSettings ++ /*releaseSettings ++*/ Seq(
    // must use same version of scala as SBT
    scalaVersion := "2.10.4",
    organization := "com.github.fommil.lion",
    version := "1.0.0-SNAPSHOT",
    // scoverage highlighting fixed in scala 2.11
    ScoverageKeys.coverageHighlighting := false,
    javacOptions in (Compile, compile) ++= Seq (
      "-source", "1.6", "-target", "1.6", "-Xlint:all", "-Werror",
      "-Xlint:-options", "-Xlint:-path", "-Xlint:-processing"
    ),
    javacOptions in doc ++= Seq("-source", "1.6"),
    scalacOptions in Compile ++= Seq(
      "-encoding", "UTF-8", "-target:jvm-1.6", "-feature", "-deprecation",
      "-Xfatal-warnings",
      "-language:postfixOps", "-language:implicitConversions"
    ),
    compileOrder := CompileOrder.JavaThenScala,
    outputStrategy := Some(StdoutOutput),
    fork := true,
    maxErrors := 1,
    licenses := Seq("LGPL" -> url("https://www.gnu.org/licenses/lgpl.html")),
    homepage := Some(url("http://github.com/fommil/lions-share")),
    // http://www.scala-sbt.org/release/docs/Community/Using-Sonatype.html#sbt-sonatype-sbt
    // don't forget to create your ~/.sbt/0.13/sonatype.sbt and ~/.sbt/0.13/plugins/gpg.sbt
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    //publishArtifactsAction := PgpKeys.publishSigned.value,
    credentials += Credentials(
      "Sonatype Nexus Repository Manager", "oss.sonatype.org",
      sys.env.get("SONATYPE_USERNAME").getOrElse(""),
      sys.env.get("SONATYPE_PASSWORD").getOrElse("")
    ),
    publishTo <<= version { v: String =>
         val nexus = "https://oss.sonatype.org/"
         if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
         else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := (
     <scm>
       <url>git@github.com:fommil/lions-share.git</url>
       <connection>scm:git:git@github.com:fommil/lions-share.git</connection>
     </scm>
     <developers>
       <developer>
         <id>fommil</id>
         <name>Sam Halliday</name>
       </developer>
     </developers>
    )
  )

  def bad = Seq(
    ExclusionRule(name = "log4j"),
    ExclusionRule(name = "commons-logging"),
    ExclusionRule(organization = "org.slf4j")
  )
}
