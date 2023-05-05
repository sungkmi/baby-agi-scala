val V = new {
  val Scala      = "3.3.0-RC5"
  val ScalaGroup = "3.3"

  val catsEffect      = "3.4.10"
  val pureconfig      = "0.17.3"
  val sttp            = "3.8.15"
  val organiseImports = "0.6.0"
  val scribe          = "3.11.1"
  val jsoniter        = "2.23.0"
}

val Dependencies = new {
  lazy val babyagi = Seq(
    libraryDependencies ++= Seq(
      "org.typelevel"                 %% "cats-effect"          % V.catsEffect,
      "com.github.pureconfig"         %% "pureconfig-core"      % V.pureconfig,
      "com.softwaremill.sttp.client3" %% "armeria-backend-cats" % V.sttp,
      "com.softwaremill.sttp.client3" %% "jsoniter"             % V.sttp,
      "com.outr"                      %% "scribe-slf4j"         % V.scribe,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % V.jsoniter,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % V.jsoniter % "compile-internal",
    ),
  )
}

ThisBuild / organization := "dev.sungkm"
ThisBuild / version      := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := V.Scala
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % V.organiseImports
ThisBuild / semanticdbEnabled := true

lazy val babyagi = (project in file("."))
  .settings(Dependencies.babyagi)
  .settings(
    name := "babyagi-scala",
  )
