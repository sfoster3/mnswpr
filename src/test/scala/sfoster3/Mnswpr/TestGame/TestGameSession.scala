package sfoster3.Mnswpr.TestGame

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import sfoster3.Mnswpr.Game.GameMessages._
import sfoster3.Mnswpr.Game.GameSession
import sfoster3.Mnswpr.Game.GameSession.{FlaggedCell, RevealedCell}
import sfoster3.Mnswpr.MineField.Coordinate

class TestGameSession
  extends TestKit(ActorSystem("testSystem"))
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll
    with ImplicitSender {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def getSession: ActorRef =
    system.actorOf(GameSession.props(1, 4, 5, 6, Coordinate(1, 2), Some(100)))

  /*
  Seed 100 ->
    X X 2 1
    X 5 X 2
    1 4 X 3
    0 2 X 2
    0 1 1 1
   */

  "A GameSession" must {
    "create a field on demand" in {
      val session = getSession
      session ! GetVisible()
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set())))
    }
  }

  "A GameSession flag" must {
    "flag a cell" in {
      val session = getSession
      session ! Flag(Coordinate(1, 1))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 5, Set(Coordinate(1, 1) -> FlaggedCell()))))
      session ! Flag(Coordinate(0, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 4, Set(
        Coordinate(1, 1) -> FlaggedCell(),
        Coordinate(0, 2) -> FlaggedCell()
      ))))
    }
    "un-flag a flagged cell" in {
      val session = getSession
      session ! Flag(Coordinate(1, 1))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 5, Set(Coordinate(1, 1) -> FlaggedCell()))))
      session ! Flag(Coordinate(1, 1))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set())))
    }
    "no-op for revealed cells" in {
      val session = getSession
      session ! Reveal(Coordinate(1, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set(Coordinate(1, 2) -> RevealedCell(4)))))
      session ! Flag(Coordinate(1, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set(Coordinate(1, 2) -> RevealedCell(4)))))
    }
  }

  "A GameSession reveal" must {
    "reveal a non-mine cell" in {
      val session = getSession
      session ! Reveal(Coordinate(1, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set(Coordinate(1, 2) -> RevealedCell(4)))))
      session ! Reveal(Coordinate(0, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set(
        Coordinate(1, 2) -> RevealedCell(4),
        Coordinate(0, 2) -> RevealedCell(1)
      ))))
    }
    "lose when revealing a mine cell" in {
      val session = getSession
      session ! Reveal(Coordinate(1, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set(Coordinate(1, 2) -> RevealedCell(4)))))
      session ! Reveal(Coordinate(2, 2))
      expectMsg(VisibleLoss(VisibleBoard(1, 4, 5, 6, Set(Coordinate(1, 2) -> RevealedCell(4))), Set(Coordinate(2, 2))))
    }
  }

  "A GameSession revealAdj" must {
    "reveal adjcent non-mine cells" in {
      val session = getSession
      session ! Reveal(Coordinate(0, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set(Coordinate(0, 2) -> RevealedCell(1)))))
      session ! Flag(Coordinate(0, 1))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 5, Set(
        Coordinate(0, 1) -> FlaggedCell(),
        Coordinate(0, 2) -> RevealedCell(1)
      ))))
      session ! RevealAdj(Coordinate(0, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 5, Set(
        Coordinate(0, 1) -> FlaggedCell(),
        Coordinate(0, 2) -> RevealedCell(1),
        Coordinate(0, 3) -> RevealedCell(0),
        Coordinate(0, 4) -> RevealedCell(0),
        Coordinate(1, 1) -> RevealedCell(5),
        Coordinate(1, 2) -> RevealedCell(4),
        Coordinate(1, 3) -> RevealedCell(2),
        Coordinate(1, 4) -> RevealedCell(1)
      ))))
    }
    "no-op on insufficiently flagged cells" in {
      val session = getSession
      session ! Reveal(Coordinate(1, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set(Coordinate(1, 2) -> RevealedCell(4)))))
      session ! Flag(Coordinate(0, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 5, Set(
        Coordinate(1, 2) -> RevealedCell(4),
        Coordinate(0, 2) -> FlaggedCell()
      ))))
      session ! RevealAdj(Coordinate(1, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 5, Set(
        Coordinate(1, 2) -> RevealedCell(4),
        Coordinate(0, 2) -> FlaggedCell()
      ))))
    }
    "lose when revealing a mine cell" in {
      val session = getSession
      session ! Reveal(Coordinate(1, 2))
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 6, Set(Coordinate(1, 2) -> RevealedCell(4)))))
      session ! Flag(Coordinate(0, 2))
      session ! Flag(Coordinate(0, 3))
      session ! Flag(Coordinate(1, 3))
      session ! Flag(Coordinate(2, 3))
      receiveN(3)
      expectMsg(VisibleResult(VisibleBoard(1, 4, 5, 2, Set(
        Coordinate(1, 2) -> RevealedCell(4),
        Coordinate(0, 2) -> FlaggedCell(),
        Coordinate(0, 3) -> FlaggedCell(),
        Coordinate(1, 3) -> FlaggedCell(),
        Coordinate(2, 3) -> FlaggedCell()
      ))))
      session ! RevealAdj(Coordinate(1, 2))
      expectMsg(VisibleLoss(VisibleBoard(1, 4, 5, 2, Set(
        Coordinate(1, 2) -> RevealedCell(4),
        Coordinate(0, 2) -> FlaggedCell(),
        Coordinate(0, 3) -> FlaggedCell(),
        Coordinate(1, 3) -> FlaggedCell(),
        Coordinate(2, 3) -> FlaggedCell()
      )), Set(
        Coordinate(0, 1), Coordinate(2, 1), Coordinate(2, 2)
      )))
    }
  }
}
