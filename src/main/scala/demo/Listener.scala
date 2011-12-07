package demo

import org.slf4j.LoggerFactory
import akka.actor.ActorRef

/** The server side listener actor.  This gets the value from the Future it gets
 * from the server, then replies to the caller with that value */
class ListenerActor  extends akka.actor.Actor {
  val logger =  LoggerFactory.getLogger(this.getClass)
  logger.info("Listen Actor inst. created")

  def receive = {
    case get @ Get =>
      logger.info("getting future")
      val future = (getServer ? get ).asInstanceOf[akka.dispatch.Future[akka.dispatch.Future[String]]]
      logger.info(this.getClass()+" waiting on future "+future)
      val replyMsg = future.get.get
      logger.info(this.getClass()+" replying with "+replyMsg)
      self reply replyMsg
    case x =>
      logger.error("I don't know what to do with "+x)
  }

  private def getServer: ActorRef = {
    //works well enough for demo purposes
    akka.actor.Actor.registry.actorsFor[Server].head
  }
}
