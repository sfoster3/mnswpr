package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.Game.GameMessages.VisibleBoard
import sfoster3.Mnswpr.Game.GameSession.{FlaggedCell, RevealedCell}
import sfoster3.Mnswpr.MineField.Coordinate

class BruteForceChecker(override val board: VisibleBoard) extends Checker {
  override def isValid(solution: Set[Coordinate]): Boolean = {
    val numCells: Set[(Coordinate, Int)] = board.cells.collect {
      case (co, RevealedCell(n)) => (co, n)
    }
    val mines: Set[Coordinate] = board.cells.collect {
      case (co, FlaggedCell()) => co
    } | solution
    numCells.forall {
      case (co, count) => co.getAdj.count(mines.contains) == count
    }
  }
}

object BruteForceChecker {
  def apply(board: VisibleBoard): BruteForceChecker =
    new BruteForceChecker(board)
}
