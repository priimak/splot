name := "splot-core"

organization := "xyz.devfortress.splot"
version := "0.5.0-SNAPSHOT"

scalaVersion := "2.13.2"
scalacOptions += "-Xfatal-warnings"
scalacOptions += "-feature"
scalacOptions += "-deprecation"
scalacOptions += "-language:implicitConversions"

pomIncludeRepository := { _ => false }

licenses := Seq("MIT-style" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("http://git.devfortress.xyz/plugins/gitiles/splot/+/master/README.md"))

scmInfo := Some(
  ScmInfo(
    url("http://git.devfortress.xyz/plugins/gitiles/splot"),
    "scm:http://git.devfortress.xyz/splot"
  )
)

developers := List(
  Developer(
    id    = "priimak",
    name  = "Dmitri Priimak",
    email = "priimak@devfortress.xyz",
    url   = url("http://www.devfortress.xyz")
  )
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

lazy val root = (project in file(".")).
  enablePlugins(ParadoxPlugin).
  settings(
    name := "Hello Project",
    paradoxTheme := Some(builtinParadoxTheme("generic"))
  )

libraryDependencies += "org.scalatest" % "scalatest_2.13" % "3.1.0-SNAP13"
libraryDependencies += "org.scilab.forge" % "jlatexmath" % "1.0.7"

coverageExcludedPackages := "<empty>;xyz\\.devfortress\\.splot\\.colormaps\\..*"
