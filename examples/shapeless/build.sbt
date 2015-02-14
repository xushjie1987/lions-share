scalaVersion := "2.10.4"

resolvers += Resolver.sonatypeRepo("staging")

resolvers += Resolver.sonatypeRepo("snapshots")

//javaOptions := Seq("-XX:+UseConcMarkSweepGC")

libraryDependencies ++= Seq(
  "com.chuusai" % "shapeless" % "2.0.0" cross CrossVersion.full,
  "org.scala-lang" % "scala-compiler" % "2.10.4"
)
