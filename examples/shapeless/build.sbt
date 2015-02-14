scalaVersion := "2.10.4"

//javaOptions := Seq("-XX:+UseConcMarkSweepGC")

libraryDependencies ++= Seq(
  "com.chuusai" % "shapeless" % "2.0.0" cross CrossVersion.full,
  "org.scala-lang" % "scala-compiler" % "2.10.4"
)
