import sbt.Keys.libraryDependencies

val zioHttpVersion = "3.0.0-RC3"

ThisBuild / organization := "homies.goat"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

val zioVersion: String = "2.0.20"

lazy val root = (project in file("."))
  .in(file("."))
  .settings(
    name := "ProjetScala",
    libraryDependencies ++= Seq (
      "dev.zio" %% "zio"         % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-http"    % zioHttpVersion
    ),
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )