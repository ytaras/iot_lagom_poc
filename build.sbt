organization in ThisBuild := "com.ss"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val `ss` = (project in file("."))
  .aggregate(`ss-mapping-api`, `ss-mapping-impl`,
    `ss-api`, `ss-impl`, `ss-stream-api`, `ss-stream-impl`)

lazy val `ss-api` = (project in file("ss-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `ss-mapping-api` = (project in file("ss-mapping-api"))
    .settings(
      libraryDependencies ++= Seq(
        lagomScaladslApi
      )
    )

lazy val `ss-mapping-impl` = (project in file("ss-mapping-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`ss-mapping-api`)
  .settings(lagomForkedTestSettings: _*)

lazy val `ss-impl` = (project in file("ss-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`ss-api`)

lazy val `ss-stream-api` = (project in file("ss-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `ss-stream-impl` = (project in file("ss-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`ss-stream-api`, `ss-api`)

resolvers += "JBoss" at "https://repository.jboss.org/"
