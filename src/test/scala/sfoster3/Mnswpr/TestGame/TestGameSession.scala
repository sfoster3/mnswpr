package sfoster3.Mnswpr.TestGame

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import sfoster3.Mnswpr.Game.GameMessages._
import sfoster3.Mnswpr.Game.GameSession
import sfoster3.Mnswpr.Game.GameSession.{FlaggedCell, MineCell, RevealedCell}
import sfoster3.Mnswpr.MineField.Conversions._
import sfoster3.Mnswpr.MineField.Coordinate
import sfoster3.Mnswpr.Web.APIActionResponse

import scala.language.implicitConversions

class TestGameSession
  extends TestKit(ActorSystem("testSystem"))
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll
    with ImplicitSender {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def getSession: ActorRef = {
    system.actorOf(GameSession.props(1, 4, 5, 6, Some(100)))
  }

  implicit def coordinateMapConvert[T](v: ((Int, Int), T)): (Coordinate, T) = v match {
    case ((x, y), t) => Coordinate(x, y) -> t
  }

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
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set())))
    }
  }

  "A GameSession flag" must {
    "flag a cell" in {
      val session = getSession
      session ! Flag(1, 1)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 5, Set((1, 1) -> FlaggedCell()))))
      session ! Flag(0, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 4, Set(
        (1, 1) -> FlaggedCell(),
        (0, 2) -> FlaggedCell()
      ))))
    }
    "un-flag a flagged cell" in {
      val session = getSession
      session ! Flag(1, 1)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 5, Set((1, 1) -> FlaggedCell()))))
      session ! Flag(1, 1)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set())))
    }
    "no-op for revealed cells" in {
      val session = getSession
      session ! Reveal(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set((1, 2) -> RevealedCell(4)))))
      session ! Flag(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set((1, 2) -> RevealedCell(4)))))
    }
  }

  "A GameSession reveal" must {
    "reveal a non-mine cell" in {
      val session = getSession
      session ! Reveal(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set((1, 2) -> RevealedCell(4)))))
      session ! Reveal(0, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set(
        (1, 2) -> RevealedCell(4),
        (0, 2) -> RevealedCell(1)
      ))))
    }
    "lose when revealing a mine cell" in {
      val session = getSession
      session ! Reveal(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set((1, 2) -> RevealedCell(4)))))
      session ! Reveal(2, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set((1, 2) -> RevealedCell(4), (2, 2) -> MineCell())), isLoss = true))
    }
  }

  "A GameSession revealAdj" must {
    "reveal adjcent non-mine cells" in {
      val session = getSession
      session ! Reveal(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set((1, 2) -> RevealedCell(4)))))
      session ! Flag(0, 1)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 5, Set(
        (0, 1) -> FlaggedCell(),
        (1, 2) -> RevealedCell(4)
      ))))
      session ! Flag(2, 1)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 4, Set(
        (0, 1) -> FlaggedCell(),
        (1, 2) -> RevealedCell(4),
        (2, 1) -> FlaggedCell()
      ))))
      session ! Flag(2, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 3, Set(
        (0, 1) -> FlaggedCell(),
        (1, 2) -> RevealedCell(4),
        (2, 1) -> FlaggedCell(),
        (2, 2) -> FlaggedCell()
      ))))
      session ! Flag(2, 3)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 2, Set(
        (0, 1) -> FlaggedCell(),
        (1, 2) -> RevealedCell(4),
        (2, 1) -> FlaggedCell(),
        (2, 2) -> FlaggedCell(),
        (2, 3) -> FlaggedCell()
      ))))
      session ! RevealAdj(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 2, Set(
        (0, 1) -> FlaggedCell(),
        (0, 2) -> RevealedCell(1),
        (0, 3) -> RevealedCell(0),
        (0, 4) -> RevealedCell(0),
        (1, 1) -> RevealedCell(5),
        (1, 2) -> RevealedCell(4),
        (1, 3) -> RevealedCell(2),
        (1, 4) -> RevealedCell(1),
        (2, 1) -> FlaggedCell(),
        (2, 2) -> FlaggedCell(),
        (2, 3) -> FlaggedCell()
      ))))
    }
    "no-op on insufficiently flagged cells" in {
      val session = getSession
      session ! Reveal(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set((1, 2) -> RevealedCell(4)))))
      session ! Flag(0, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 5, Set(
        (1, 2) -> RevealedCell(4),
        (0, 2) -> FlaggedCell()
      ))))
      session ! RevealAdj(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 5, Set(
        (1, 2) -> RevealedCell(4),
        (0, 2) -> FlaggedCell()
      ))))
    }
    "lose when revealing a mine cell" in {
      val session = getSession
      session ! Reveal(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 6, Set((1, 2) -> RevealedCell(4)))))
      session ! Flag(0, 2)
      session ! Flag(0, 3)
      session ! Flag(1, 3)
      session ! Flag(2, 3)
      receiveN(3)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 2, Set(
        (1, 2) -> RevealedCell(4),
        (0, 2) -> FlaggedCell(),
        (0, 3) -> FlaggedCell(),
        (1, 3) -> FlaggedCell(),
        (2, 3) -> FlaggedCell()
      ))))
      session ! RevealAdj(1, 2)
      expectMsg(APIActionResponse(VisibleBoard(1, 4, 5, 2, Set(
        (0, 1) -> MineCell(),
        (1, 2) -> RevealedCell(4),
        (0, 2) -> FlaggedCell(),
        (0, 3) -> FlaggedCell(),
        (1, 3) -> FlaggedCell(),
        (2, 1) -> MineCell(),
        (2, 2) -> MineCell(),
        (2, 3) -> FlaggedCell()
      )), isLoss = true))
    }
  }
}
