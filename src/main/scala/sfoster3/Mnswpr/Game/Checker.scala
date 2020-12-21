package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.Game.GameMessages.VisibleBoard
import sfoster3.Mnswpr.MineField.Coordinate

trait Checker {
  val board: VisibleBoard

  def isValid(solution: Set[Coordinate]): Boolean
}
