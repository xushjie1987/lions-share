import sbt._
import Keys._
import Package.ManifestAttributes
import sbtassembly.Plugin._
import AssemblyKeys._
import com.typesafe.sbt.SbtScalariform._
import scoverage.ScoverageSbtPlugin._

object LionBuild extends FommilBuild with Dependencies {

  override def projectOrg = "com.github.fommil.lion"
  override def projectVersion = "1.0-SNAPSHOT"

  lazy val agent = Project(id = "agent", base = file("agent"), settings = defaultSettings ++ assemblySettings) settings (
    // all this for a pure java module...
    autoScalaLibrary := false, crossPaths := false,
    libraryDependencies ++= Seq(lombok, allocInstrument, guava),
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
  ) settings (
    addArtifact(artifact in (Compile, assembly), assembly).settings: _*
  )

  lazy val analysis = module("analysis") settings (
    libraryDependencies ++= sprayjson :: parboiled :: commonsMaths :: akka :: logback :: guava :: jsr305 :: scalatest :: Nil)

  lazy val sbt = module("sbt") dependsOn (analysis) settings (
    sbtPlugin := true)

  def modules = List(agent, analysis, sbt)
  def top = sbt
}

trait Dependencies {
  val bad = Seq(
    ExclusionRule(name = "log4j"),
    ExclusionRule(name = "commons-logging"),
    ExclusionRule(organization = "org.slf4j")
  )

  val lombok = "org.projectlombok" % "lombok" % "1.16.2" % "provided"
  val allocInstrument = "com.github.fommil" % "java-allocation-instrumenter" % "3.0"
  val guava = "com.google.guava" % "guava" % "18.0"
  // guava doesn't declare jsr305
  val jsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.3"

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"

  val sprayjson = "io.spray" %% "spray-json" % "1.3.1"
  val parboiled = "org.parboiled" %% "parboiled-scala" % "1.1.7"
  val akka = "com.typesafe.akka" %% "akka-slf4j" % "2.3.9" excludeAll (bad: _*)

  val logback = "ch.qos.logback" % "logback-classic" % "1.1.2"

  val commonsMaths = "org.apache.commons" % "commons-math3" % "3.4.1"
}

trait FommilBuild extends Build {

  def projectVersion = "1.0-SNAPSHOT"
  def projectOrg = "com.github.fommil"
  def projectScala = "2.10.4"
  def modules: List[ProjectReference]
  def top: ProjectReference

  override val settings = super.settings ++ Seq(
    organization := projectOrg,
    version := projectVersion
  )
  def module(dir: String) = Project(id = dir, base = file(dir), settings = defaultSettings)

  lazy val defaultSettings = Defaults.defaultSettings ++ scalariformSettings ++ Seq(
    // fixed in scala 2.11
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
//    resolvers ++= Seq(
//      Resolver.mavenLocal,
//      Resolver.typesafeRepo("releases"),
//      Resolver.sonatypeRepo("snapshots")
//      "spray" at "http://repo.spray.io/"
//    ),
    scalaVersion := projectScala,
    licenses := Seq("LGPL" -> url("https://www.gnu.org/licenses/lgpl.html")),
    homepage := Some(url("http://github.com/fommil/lions-share")),
    // http://www.scala-sbt.org/release/docs/Community/Using-Sonatype.html#sbt-sonatype-sbt
    // don't forget to create your ~/.sbt/0.13/sonatype.sbt and ~/.sbt/0.13/plugins/gpg.sbt
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
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

  // would be nice not to have to define the 'root'
  lazy val root = Project(id = "parent", base = file("."), settings = defaultSettings) aggregate (modules: _*) dependsOn (top)

}
