package sfoster3.Mnswpr.Game
import sfoster3.Mnswpr.Game.GameMessages.VisibleBoard
import sfoster3.Mnswpr.Game.GameSession.{RevealedCell, UnknownCell}
import sfoster3.Mnswpr.MineField.Coordinate

import scala.collection.mutable

object EdgeSolver extends Solver {

  private def floodEdge(
      startCell: Coordinate,
      edgeCells: Set[Coordinate]
  ): Set[Coordinate] = {
    val edge: mutable.Set[Coordinate] = mutable.Set(startCell)
    val adj = mutable.Set.from(startCell.getAdj.intersect(edgeCells))
    val rem = mutable.Set.from(edgeCells)
    adj.foreach(a => {
      rem.remove(a)
      val flooded = floodEdge(a, rem.toSet)
      edge.addAll(flooded | Set(a))
      rem.filterInPlace(!flooded.contains(_))
    })
    edge.toSet
  }

  private def getEdges(board: VisibleBoard): Seq[Set[Coordinate]] = {
    val edges: mutable.Buffer[Set[Coordinate]] = mutable.Buffer()

    // Flood fill
    val knownCells: Set[Coordinate] = board.cells.collect {
      case (co, cell) if cell != UnknownCell() => co
    }
    val edgeCells: mutable.Set[Coordinate] = mutable.Set.from(
      board.cells
        .collect {
          case (co, RevealedCell(num)) if num > 0 => co
        }
        .flatMap(_.getAdj)
        .diff(knownCells)
    )
    while (edgeCells.nonEmpty) {
      val startCell = edgeCells.head
      edgeCells.remove(startCell)
      val newEdge = floodEdge(startCell, edgeCells.toSet)
      edges.addOne(newEdge)
      edgeCells.filterInPlace(!newEdge.contains(_))
    }
    edges.toSeq
  }

  override def solve(
      board: VisibleBoard
  ): Map[Coordinate, Float] = {
    val edges = getEdges(board)
    throw new scala.NotImplementedError()
  }
}
