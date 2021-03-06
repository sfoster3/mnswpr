package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.MineField.{Coordinate, MineField}

import scala.util.Random

object SafeBufferGenerator extends MineFieldGenerator {
  def doGeneration(
      start: Coordinate,
      width: Int,
      height: Int,
      count: Int,
      random: Random
  ): MineField = {
    val safeZone: Set[Coordinate] = start.getAdj + start
    val mines = Iterator
      .continually {
        Coordinate(random.between(0, width - 1), random.between(0, height - 1))
      }
      .filter(!safeZone.contains(_))
      .distinct
      .take(count)
      .toSet
    MineField(width, height, mines)
  }
}
