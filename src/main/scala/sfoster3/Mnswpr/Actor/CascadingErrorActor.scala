package sfoster3.Mnswpr.Actor

import akka.actor.{Actor, Status}

trait CascadingErrorActor extends Actor {
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    sender() ! Status.Failure(reason)
  }
}
