package sfoster3.Mnswpr.Game

import akka.actor.{ActorRef, Props}
import sfoster3.Mnswpr.Actor.CascadingErrorActor
import sfoster3.Mnswpr.Actor.Errors.NotFoundException
import sfoster3.Mnswpr.Game.GameMessages._

object GameBroker {
  def props: Props = Props(new GameBroker)
}

class GameBroker extends CascadingErrorActor {

  private def wrapReceive(games: Map[Int, ActorRef], idx: Int): Receive = {
    case CreateGame(width, height, count, seed) =>
      val gameId = idx + 1
      val gameSession = context.actorOf(GameSession.props(gameId, width, height, count, seed))
      sender() ! GameCreated(gameId)
      context.become(wrapReceive(games.updated(gameId, gameSession), gameId))
    case DeleteGame(gameId) =>
      sender() ! GameDeleted(gameId)
      context.become(wrapReceive(games.removed(gameId), idx))
    case BrokerMessage(gameId, message) => games.get(gameId) match {
      case Some(gameSession) => gameSession.forward(message)
      case None => throw new NotFoundException(s"No game with id:$gameId")
    }
  }

  override def receive: Receive = wrapReceive(Map(), 1)
}
