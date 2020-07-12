name := "server"

version := "0.1"

scalaVersion := "2.11.12"

//logLevel := Level.Debug

//mainClass in (Compile,run) := Some("co.winda.Winda")

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

resolvers += Resolver.jcenterRepo
resolvers += Resolver.sonatypeRepo("public")
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += Resolver.bintrayRepo("projectseptemberinc", "maven")
resolvers += "Bartek's repo at Bintray" at "https://dl.bintray.com/btomala/maven"
resolvers += "Kingsley Hendrickse's repo" at "https://dl.bintray.com/kingsleyh/repo/"

dependencyOverrides ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.18",
  "com.typesafe.akka" %% "akka-stream" % "2.5.18",
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.18" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.18" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test,
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "com.typesafe.akka" %% "akka-actor" % "2.5.18",
  "com.typesafe.akka" %% "akka-stream" % "2.5.18",
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.18" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.18" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test,
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "com.liyaos" %% "scala-forklift-slick" % "0.3.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
  "com.github.tminglei" %% "slick-pg" % "0.16.3",
  "com.github.tminglei" %% "slick-pg_jts" % "0.16.3",
  "com.github.tminglei" %% "slick-pg_spray-json" % "0.16.3",
  "io.strongtyped" %% "active-slick" % "0.3.5",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.17" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.3" % Test,
  "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-beta7",
  "org.deeplearning4j" % "deeplearning4j-scaleout" % "1.0.0-beta7",
  "org.deeplearning4j" % "deeplearning4j-data" % "1.0.0-beta7",
  "org.deeplearning4j" % "deeplearning4j-ui" % "1.0.0-beta7" exclude("org.slf4j", "slf4j-log4j12"),
  "org.deeplearning4j" % "deeplearning4j-ui-standalone" % "1.0.0-beta7" exclude("org.slf4j", "slf4j-log4j12"),
//  "org.deeplearning4j" % "deeplearning4j-ui-resources" % "1.0.0-beta7",
  "org.deeplearning4j" % "arbiter" % "1.0.0-beta7",
  "org.deeplearning4j" % "arbiter-deeplearning4j" % "1.0.0-beta7",
  "org.deeplearning4j" % "arbiter-ui" % "1.0.0-beta7",
  // "org.deeplearning4j" % "arbiter-ui_2.11" % "1.0.0-beta5",
  "org.deeplearning4j" % "arbiter-server" % "1.0.0-beta7",
  "org.deeplearning4j" % "rl4j" % "1.0.0-beta7",
  "org.nd4j" % "nd4j-native-platform" % "1.0.0-beta7",
  "fr.iscpif" %% "mgo" % "2.3",
  "com.hunorkovacs" %% "koauth" % "2.0.0",
  "io.jenetics" % "jenetics" % "4.3.0",
  "org.bouncycastle" % "bcprov-jdk16" % "1.46",
  "io.github.jmcardon" %% "tsec-common" % "0.0.1-M11",
  "io.github.jmcardon" %% "tsec-password" % "0.0.1-M11",
  "io.github.jmcardon" %% "tsec-mac" % "0.0.1-M11",
  "io.github.jmcardon" %% "tsec-signatures" % "0.0.1-M11",
  "io.github.jmcardon" %% "tsec-hash-jca" % "0.0.1-M11",
  "com.coiney" %% "akka-mailer-core" % "0.2.0",
  "com.coiney" %% "akka-mailer-smtp" % "0.2.0",
  "net.kenro-ji-jin" %% "erika" % "65",
  "net.debasishg" %% "redisreact" % "0.9",
  "org.slf4j" % "slf4j-nop" % "1.7.26",
  "org.reflections" % "reflections" % "0.9.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback" % "0.5"
)