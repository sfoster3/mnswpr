package sfoster3.Mnswpr.TestGame

import akka.actor.ActorSystem
import akka.actor.Status.Failure
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import sfoster3.Mnswpr.Actor.Errors.NotFoundException
import sfoster3.Mnswpr.Game.GameBroker
import sfoster3.Mnswpr.Game.GameMessages._
import sfoster3.Mnswpr.MineField.Conversions._

class TestGameBroker
  extends TestKit(ActorSystem("testSystem"))
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll
    with ImplicitSender {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A GameBroker" must {
    "create new games" in {
      val broker = system.actorOf(GameBroker.props)
      broker ! CreateGame(10, 10, 10)
      expectMsg(GameCreated(2))
      broker ! CreateGame(5, 5, 2)
      expectMsg(GameCreated(3))
    }

    "reject missing ids" in {
      val broker = system.actorOf(GameBroker.props)
      broker ! BrokerMessage(10, Reveal(1, 1))
      val failure = expectMsgType[Failure]
      assert(failure.cause.isInstanceOf[NotFoundException])
    }

    "forward messages" in {
      val broker = system.actorOf(GameBroker.props)
      broker ! CreateGame(10, 10, 10)
      expectMsg(GameCreated(2))
      broker ! BrokerMessage(2, GetVisible())
      expectMsgType[VisibleBoard]
    }

    "delete sessions" in {
      val broker = system.actorOf(GameBroker.props)
      broker ! CreateGame(10, 10, 10)
      expectMsg(GameCreated(2))
      broker ! DeleteGame(2)
      expectMsg(GameDeleted(2))
    }
  }

}
