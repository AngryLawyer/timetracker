ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.angrylawyer"

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "http://oss.sonatype.org/content/repositories/releases")

lazy val timetracker = (project in file("."))
  .settings(
    name := "TimeTracker",
    libraryDependencies += "com.googlecode.lanterna" % "lanterna" % "3.0.1"
  )
