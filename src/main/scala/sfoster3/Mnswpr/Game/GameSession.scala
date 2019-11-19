package sfoster3.Mnswpr.Game

import akka.actor.Props
import sfoster3.Mnswpr.Actor.CascadingErrorActor
import sfoster3.Mnswpr.Game.GameMessages._
import sfoster3.Mnswpr.Game.GameSession._
import sfoster3.Mnswpr.MineField.{Coordinate, MineField}
import sfoster3.Mnswpr.Web.{APIActionResponse, ResultType}


object GameSession {

  abstract class Cell()

  sealed case class UnknownCell() extends Cell

  sealed case class FlaggedCell() extends Cell

  sealed case class RevealedCell(num: Int) extends Cell

  sealed case class MineCell() extends Cell

  private sealed case class Board(private val cells: Map[Coordinate, Cell]) {
    def updated(coordinate: Coordinate, cell: Cell): Board = Board(cells.updated(coordinate, cell))

    def removed(coordinate: Coordinate): Board = Board(cells.removed(coordinate))

    def apply(coordinate: Coordinate): Cell = cells.getOrElse(coordinate, UnknownCell())
  }

  def props(gameId: Int, width: Int, height: Int, count: Int, seed: Option[Int] = None, generator: MineFieldGenerator = DefaultGenerator): Props =
    Props(new GameSession(gameId, width, height, count, seed, generator))
}

class GameSession(gameId: Int, width: Int, height: Int, count: Int, seed: Option[Int] = None, generator: MineFieldGenerator = DefaultGenerator)
  extends CascadingErrorActor {

  private def generateMineField(start: Coordinate): MineField = {
    generator.generateMineField(start, width, height, count, seed)
  }

  private def revealAndUpdate(mineField: MineField, board: Board, coordinates: Set[Coordinate]): Unit = {
    val possibleCells: Map[Coordinate, Option[Cell]] = coordinates.map {
      c =>
        c ->
          (mineField.getNum(c) match {
            case Some(n) => Some(RevealedCell(n))
            case None => None
          })
    }.toMap
    val mines: Set[Coordinate] = possibleCells.collect { case (co: Coordinate, None) => co }.toSet
    if (mines.nonEmpty) {
      sender() ! getVisibleBoard(board, Some(mines))
    } else {
      @scala.annotation.tailrec
      def _flood(cells: Map[Coordinate, Cell]): Map[Coordinate, Cell] = {
        val newCells: Map[Coordinate, Cell] = cells.collect {
          case (co: Coordinate, RevealedCell(0)) => co -> None
        }.keys.flatMap {
          mineField.getAdj
        }.map { co => co -> mineField.getNum(co) }.collect {
          case (co: Coordinate, Some(n)) if !cells.contains(co) => co -> RevealedCell(n)
        }.toMap
        if (newCells.isEmpty) {
          cells
        } else {
          _flood(cells ++ newCells)
        }
      }

      val newCells: Map[Coordinate, Cell] = _flood(possibleCells.collect { case (co: Coordinate, Some(cell)) => co -> cell })
      val newBoard = newCells.foldLeft(board) { case (b: Board, (co: Coordinate, cell: Cell)) => b.updated(co, cell) }
      sender() ! getVisibleBoard(newBoard)
      context.become(wrapReceive(Some(mineField), newBoard))
    }
  }

  private def getVisibleBoard(board: Board, mines: Option[Set[Coordinate]] = None): APIActionResponse = {
    val mineSet: Set[Coordinate] = mines.getOrElse(Set())
    val cells: Map[Coordinate, Cell] = (for {
      x <- 0 until width
      y <- 0 until height
    } yield Coordinate(x, y)).collect {
      case c if mineSet.contains(c) => c -> MineCell()
      case c if board(c) != UnknownCell() => c -> board(c)
    }.toMap
    val revealedCount = cells.values.count({ case RevealedCell(_) => true case _ => false })
    val remaining = count - cells.values.count(_ == FlaggedCell())
    val done = mines.isEmpty && (revealedCount == (width * height) - count)
    val result = (mines.nonEmpty, done) match {
      case (true, _) => ResultType.Loss
      case (false, true) => ResultType.Win
      case (false, false) => ResultType.None
    }
    APIActionResponse(VisibleBoard(gameId, width, height, remaining, cells.toSet), result)
  }

  private def wrapReceive(pField: Option[MineField], board: Board): Receive = {
    case Flag(coordinate) => board(coordinate) match {
      case UnknownCell() =>
        val newBoard = board.updated(coordinate, FlaggedCell())
        sender() ! getVisibleBoard(newBoard)
        context.become(wrapReceive(pField, newBoard))
      case FlaggedCell() =>
        val newBoard = board.removed(coordinate)
        sender() ! getVisibleBoard(newBoard)
        context.become(wrapReceive(pField, newBoard))
      case RevealedCell(_) => sender() ! getVisibleBoard(board)
    }
    case Reveal(coordinate: Coordinate) => board(coordinate) match {
      case UnknownCell() =>
        val mineField = pField.getOrElse(generateMineField(coordinate))
        revealAndUpdate(mineField, board, Set(coordinate))
      case RevealedCell(_) | FlaggedCell() => sender() ! getVisibleBoard(board)
    }
    case RevealAdj(coordinate: Coordinate) => board(coordinate) match {
      case RevealedCell(n) if n != 0 =>
        val mineField = pField.getOrElse(generateMineField(coordinate))
        val adj = mineField.getAdj(coordinate)
        if (adj.count(a => board(a) == FlaggedCell()) == n) {
          revealAndUpdate(mineField, board, adj.filter(a => board(a) == UnknownCell()))
        } else {
          sender() ! getVisibleBoard(board)
        }
      case RevealedCell(0) | UnknownCell() | FlaggedCell() => sender() ! getVisibleBoard(board)
    }
    case GetVisible() => sender() ! getVisibleBoard(board)
  }

  override def receive: Receive = wrapReceive(None, Board(Map()))
}
