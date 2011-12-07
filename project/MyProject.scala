import sbt._

import sbt._
import Keys._

object MyBuild extends Build{

//We need to save the classpath to a text file the test can find, because the test
//needs to know what classpath to pass to the jvm's is starts up, so have test
//depend on creating the file

  val generateClasspathFile = TaskKey[Unit]("generate-classpath-file")

  lazy val root = Project("root", file(".")) settings(
    generateClasspathFile <<= ( dependencyClasspath in Runtime,
                               classDirectory in Compile) map { (dc, dir) =>
                                 val file = new File(dir, "classpath.txt")
                                 val pw = new java.io.PrintWriter(file)
                                 pw.println( dc.map( _.data.toString).mkString(":"))
                                 pw.close() },
    test <<= ( generateClasspathFile, test in Test) map { (_, _) => }
  )
}
