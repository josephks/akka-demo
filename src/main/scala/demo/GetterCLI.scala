package demo

import org.slf4j.LoggerFactory
import akka.actor.ActorRef

/** Gets the value from the server, prints it to stdout, and exits */
object GetterCLI extends App{

  def doReq(actor: ActorRef) = {
    implicit val timeout = //buggy: new akka.actor.Actor.Timeout(akka.util.Duration.Inf)
      new akka.actor.Actor.Timeout(10, java.util.concurrent.TimeUnit.DAYS)
    val future = actor ? Get
    val result = future.get
    result
  }

  private def usage(): Nothing = {
    Console.err.println("usage: "+this.getClass.getName()+" [host:]port ")
    System.exit(1)
    throw new Exception("can't get here")
  }
  def getRemoteActor(args: Array[String]) = {
    val HostPort = new scala.util.matching.Regex("""(\w+:)?(\d+)""")
    args match{
      case Array(HostPort(host, port)) =>  akka.actor.Actor.remote.actorFor( Server.listenerServiceId , Long.MaxValue,
                                                                            { if (host == null || host.length == 0) "localhost" else host} , port.toInt)
      case _ => usage()
    }
  }

  override def main(args: Array[String]): Unit = {
    val HostPort = new scala.util.matching.Regex("""(\w+:)?(\d+)""")
    val actor = getRemoteActor(args)
    System.out.println(doReq(actor))
    System.out.flush()
    System.exit(0) //necessary, or jvm won't exit
  }
}
