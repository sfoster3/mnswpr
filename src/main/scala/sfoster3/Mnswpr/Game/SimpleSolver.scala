package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.Game.GameMessages.VisibleBoard
import sfoster3.Mnswpr.Game.GameSession.{FlaggedCell, RevealedCell, UnknownCell}
import sfoster3.Mnswpr.MineField.Coordinate

import scala.collection.View

object SimpleSolver extends Solver {
  override def solve(board: VisibleBoard): Map[Coordinate, Float] = {

    def getAdj(coordinate: Coordinate): Set[Coordinate] =
      coordinate.getAdj.filter(board.inBounds)

    val mines: Set[Coordinate] = board.cells.collect {
      case (co, FlaggedCell()) => co
    }
    val existing = board.cells.collect {
      case (co, cell) if cell != UnknownCell() => co
    }
    val vacuums: Set[(Coordinate, Int)] = board.cells
      .collect {
        case (co, RevealedCell(num)) if num > 0 => (co, num)
      }
      .map {
        case (co, num) => (co, num - getAdj(co).intersect(mines).size)
      }
      .filter(_._2 > 0)
    val choices: View[Set[Coordinate]] = vacuums.view.collect {
      case (co, remaining) if getAdj(co).diff(existing).size == remaining =>
        getAdj(co).diff(existing)
    }
    choices.flatten.map(_ -> 1f).toMap
  }
}
