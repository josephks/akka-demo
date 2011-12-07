/** Project */
name := "Akka Getter/Setter Demo"

version := "0.1"

organization := "net.tupari"

scalaVersion := "2.9.1"

/** Shell */
shellPrompt := { state => System.getProperty("user.name") + "> " }

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

libraryDependencies += "se.scalablesolutions.akka" % "akka-actor" % "1.3-RC2"

libraryDependencies += "se.scalablesolutions.akka" % "akka-remote" % "1.3-RC2"

libraryDependencies += "org.specs2" %% "specs2" % "1.6.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" %  "0.9.28"

scalacOptions += "-deprecation"

maxErrors := 20

pollInterval := 1000

testFrameworks += new TestFramework("org.specs2.runner.SpecsFramework")

parallelExecution in Test := false

testOptions := Seq(Tests.Filter(s =>
  Seq("Spec", "Suite", "Unit", "Specs", "Test", "all").exists(s.endsWith(_)) &&
    ! s.endsWith("FeaturesSpec") ||
    s.contains("UserGuide") || 
    s.matches("org.specs2.guide.*")))

/** Console */
initialCommands in console := "import org.specs2._"
