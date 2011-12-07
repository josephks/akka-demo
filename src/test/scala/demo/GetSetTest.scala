import org.specs2.mutable._

class GetSetSpec extends Specification {

  //These eventually()s don't seem to work, and these tests hang forever

  "A Getter" should {
    "Get the value set if called before set" in  new serverContext{

      val msg = new java.util.Date().toString

      val stream1 = Process("demo.GetterCLI", portNum.toString).lines 
      println("after kicking off getter")
      Thread.sleep(1500)
      println("about to kick off setter")
      Process("demo.SetterCLI", portNum.toString, msg).run

      //filter out log lines
      stream1.filter(s => ! s.startsWith("[")).head must be_==(msg).eventually(1, 1 second)
    }
    "Get the value set if called after set" in  new serverContext{

      val msg = new java.util.Date().toString

      Process("demo.SetterCLI", portNum.toString, msg).run
      Thread.sleep(1500)
      val stream1 = Process("demo.GetterCLI", portNum.toString).lines

      stream1.filter(s => ! s.startsWith("[")).head must be_==(msg).eventually(1, 1 second)
    }
    "Get the value set if called before set with multiple getters" in  new serverContext{

      val msg = new java.util.Date().toString

      val streams = (1 to 10) map{ _ =>
        Process("demo.GetterCLI", portNum.toString).lines
      }
      Thread.sleep(2000)
      Process("demo.SetterCLI", portNum.toString, msg).run

      ((str:String) =>  str must be_==(msg).eventually(1, 1 second) ).forall(streams map( stream => stream.filter(s => ! s.startsWith("[")).head)  )

    }
  }

}// FactorySpec


private object PortGen{
  private val counter = new _root_.java.util.concurrent.atomic.AtomicInteger(2222)
  def nextPort = counter.incrementAndGet()
}


trait serverContext extends After {
  val portNum = PortGen.nextPort
  val classpath = {
    val br = new java.io.BufferedReader(new java.io.InputStreamReader(this.getClass().getResourceAsStream("classpath.txt")))
    val line = br.readLine
    br.close
    line
  }

  val proc = Process("demo.ServerMain", portNum.toString).run
  def after = {
    println("after: destroying server proc")
    proc.destroy //this doesn't seem to work, the process hangs out until sbt dies
    //This means you can't run two test runs in the same sbt session because the ports
    //repeat, and new clients will connect with old servers
    println("after: done")
  }

  def Process(args: String*) = {
    val args2 = List("scala","-cp", classpath) ++ args
    val ans = scala.sys.process.Process(args2)
    ans
  }

}
