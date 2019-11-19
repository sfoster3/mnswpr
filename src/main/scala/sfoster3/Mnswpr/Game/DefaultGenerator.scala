package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.MineField.{Coordinate, MineField}

import scala.util.Random

object DefaultGenerator extends MineFieldGenerator {
  def doGeneration(start: Coordinate, width: Int, height: Int, count: Int, random: Random): MineField = {
    val mines = Iterator.continually {
      Coordinate(random.between(0, width - 1), random.between(0, height - 1))
    }.filter(_ != start).distinct.take(count).toSet
    MineField(width, height, mines)
  }
}
