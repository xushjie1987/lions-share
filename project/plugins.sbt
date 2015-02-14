// sbt-release broken with recent sbt
// https://github.com/sbt/sbt-release/issues/98
// and recent pgp-sbt
// https://github.com/sbt/sbt-release/issues/99
//addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7.1")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.0.2")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.0.0.BETA1")
