package sfoster3.Mnswpr.TestGame

import org.scalatest.{Matchers, WordSpecLike}
import sfoster3.Mnswpr.Game.GameMessages.VisibleBoard
import sfoster3.Mnswpr.Game.GameSession.{FlaggedCell, RevealedCell}
import sfoster3.Mnswpr.Game.SimpleSolver
import sfoster3.Mnswpr.MineField.Coordinate
import sfoster3.Mnswpr.MineField.CoordinateConverters._

class TestSimpleSolver extends Matchers with WordSpecLike {
  "A SimpleSolver" must {
    "solve 1 corners" in {
      val visibleBoard = VisibleBoard(
        0,
        2,
        2,
        1,
        Set(
          ((0, 0), RevealedCell(1)),
          ((0, 1), RevealedCell(1)),
          ((1, 0), RevealedCell(1))
        )
      )
      val solution = SimpleSolver.solve(visibleBoard)
      solution should be(Map(Coordinate(1, 1) -> 1f))
    }
  }

  "solve 2 corners" in {
    val visibleBoard = VisibleBoard(
      0,
      3,
      2,
      1,
      Set(
        ((0, 1), RevealedCell(1)),
        ((1, 1), RevealedCell(2)),
        ((2, 0), RevealedCell(2)),
        ((2, 1), FlaggedCell())
      )
    )
    val solution = SimpleSolver.solve(visibleBoard)
    solution should be(Map(Coordinate(1, 0) -> 1f))
  }
}
