package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.Game.GameSession.Cell
import sfoster3.Mnswpr.MineField.Coordinate

object GameMessages {

  /* Messages To/From GameSession */
  abstract class GameMessage()

  abstract class GameResult

  sealed case class Flag(coordinate: Coordinate) extends GameMessage

  sealed case class Reveal(coordinate: Coordinate) extends GameMessage

  sealed case class RevealAdj(coordinate: Coordinate) extends GameMessage

  sealed case class Solve() extends GameMessage

  sealed case class GetVisible() extends GameMessage

  /* Messages to/from GameBroker */
  sealed case class CreateGame(
      width: Int,
      height: Int,
      count: Int,
      seed: Option[Int] = None
  )

  sealed case class GameCreated(gameId: Int)

  sealed case class DeleteGame(gameId: Int)

  sealed case class GameDeleted(gameId: Int)

  sealed case class BrokerMessage(gameId: Int, message: GameMessage)

  case class VisibleBoard(
      gameId: Int,
      width: Int,
      height: Int,
      remainingCount: Int,
      cells: Set[(Coordinate, Cell)],
      stats: Option[Set[(Coordinate, Float)]] = None
  ) extends GameResult {
    def inBounds(coordinate: Coordinate): Boolean =
      coordinate.x >= 0 && coordinate.x < width && coordinate.y >= 0 && coordinate.y < height
  }

}
