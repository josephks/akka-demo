package demo

import org.slf4j.LoggerFactory
import akka.actor.ActorRef


object SetterCLI  extends App{

  private def usage(): Nothing = {
    Console.err.println("usage: "+this.getClass.getName()+" [host:]port msg")
    System.exit(1)
    throw new Exception("can't get here")
  }
  override def main(args: Array[String]): Unit = {
    val HostPort = new scala.util.matching.Regex("""(\w+:)?(\d+)""")
    val actor: ActorRef = args match{
      case Array(HostPort(host, port), _, _*) =>  akka.actor.Actor.remote.actorFor( Server.serviceId, { if (host == null || host.length == 0) "localhost" else host} , port.toInt)
      case _ => usage()
    }

    val message = args.slice(1, args.length).mkString(" ")
    actor  !  Set( message )
    System.out.println("set to: "+message) //since this is a CLI print to console
    System.exit(0)
  }
}
