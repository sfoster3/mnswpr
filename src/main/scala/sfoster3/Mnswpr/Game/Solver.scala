package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.Game.GameMessages.VisibleBoard
import sfoster3.Mnswpr.MineField.Coordinate

trait Solver {
  def solve(board: VisibleBoard): Map[Coordinate, Float]
}
