package demo

import org.slf4j.LoggerFactory
import akka.actor.ActorRef

class Server  extends akka.actor.Actor {

  val logger = Server.logger
  logger.info("Server inst. created")

  var waiters : List[ akka.dispatch.DefaultCompletableFuture[String]] = List()

  /** Data storage */
  var value : Option[String] = None

  def receive = {
    //If we get a Set, notify all the waiters
    case Set(x) =>
      value = Option(x)
    logger.info("Received: Set("+x+")");
    waiters.par.foreach {
      replyFuture =>
        //using this "myself" instead of "self" seemed to solve the problem, but apparently it didn't :(
        //val myself = akka.actor.Actor.registry.actorFor(self.uuid).get

      //spin off thread.  We can't send a message to ourself in this thread bec. only one message can be processed at a time,
        //and we'll hang on getting the reply from ourself
      scala.actors.Actor.actor{
        logger.info("before self ? req. self is: "+self+" (class "+self.getClass+")")
        val akkaReplyFuture =  self ? Get
        logger.info("   akkaReplyFuture is: " +   akkaReplyFuture+" complete: "+akkaReplyFuture.isCompleted)
        val insideFuture =  akkaReplyFuture.get.asInstanceOf[akka.dispatch.Future[String]]
        logger.info("before replyFuture.completeWith")
        replyFuture.completeWith ( insideFuture )
        logger.info("set replyFuture.completeWith")
      }
    }
    logger.info("clearing watiers")
    waiters = List()
    case Get =>
      logger.info("recieved Get")
      //reply with a Future of some kind, depending on if we have data set or now
    value match {
      case None =>  //no data to return right now, block
        logger.info("no data, adding to waiters")
      val ans  =  new akka.dispatch.DefaultCompletableFuture[String] (10, java.util.concurrent.TimeUnit.DAYS)
      waiters = ans :: waiters
      self reply ans
      case Some(msg) =>
        logger.info(" replying to Get with " + value)
      self reply new akka.dispatch.AlreadyCompletedFuture(
        Either.cond (true,  msg, null )
      )
    }
    case x =>   logger.info(" I don't know what to do with " + x)
  }

}

object Server {
  val logger = LoggerFactory.getLogger(Server.getClass);  //slf4j can't handle scala objects as arguments to getLogger(class)
  val serviceId = "getset-demo-service"
  val listenerServiceId = "listener-service"
}


//Making Get a case class resulted in a compiler error I don't understand, but we don't need it to hold any state so it can be an Object
object Get extends java.io.Serializable { }
case class Set(x: String)



object ServerMain extends App{

  var host = ""
  var port = -1

  private def start(host: String, port: Int){
    this.host = host
    this.port = port
    akka.actor.Actor.remote.start(host, port)
  }
  private def usage = {
    Console.err.println("usage: demo.ServerMain [host] port")
    System.exit(1)
  }
  override def main(args: Array[String]): Unit = {
    args match{
      case Array(host, port) => start(host, port.toInt)
      case Array(port) =>  start("localhost", port.toInt)
      case _ => usage
    }
    //clients that are posting messages connect directly to the server
    akka.actor.Actor.remote.register(Server.serviceId, akka.actor.Actor.actorOf[Server]) //Register the actor with the specified service id
    //client getting values use this
    akka.actor.Actor.remote.registerPerSession(  Server.listenerServiceId , akka.actor.Actor.actorOf(  new ListenerActor ))

    Server.logger.info("registered actors")
  }
}
