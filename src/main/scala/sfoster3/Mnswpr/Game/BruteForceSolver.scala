package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.Game.GameSession.UnknownCell
import sfoster3.Mnswpr.MineField.Coordinate

object BruteForceSolver extends Solver {

  override def solve(
      board: GameMessages.VisibleBoard
  ): Map[Coordinate, Float] = {
    val checker = BruteForceChecker(board)
    val existing = board.cells.collect {
      case (co, cell) if cell != UnknownCell() => co
    }
    val candidates = (for {
      x <- 0 until board.width
      y <- 0 until board.height
    } yield Coordinate(x, y)).filterNot(existing.contains).toSet
    val remaining = board.remainingCount
    val selections: Iterator[Set[Coordinate]] = candidates
      .subsets(remaining)
      .filter(checker.isValid)
    val counts: (Map[Coordinate, Int], Int) = selections.foldLeft(
      (Map[Coordinate, Int](), 0)
    )((tu: (Map[Coordinate, Int], Int), set: Set[Coordinate]) => {
      val map = tu._1
      val keys = map.keys.toSet | set
      keys.map {
        case co if map.contains(co) =>
          co -> (map(co) + (if (set.contains(co)) 1 else 0))
        case co => co -> 1
      }.toMap -> (tu._2 + 1)
    })
    counts._1.view.mapValues(_.toFloat / counts._2).toMap
  }
}
