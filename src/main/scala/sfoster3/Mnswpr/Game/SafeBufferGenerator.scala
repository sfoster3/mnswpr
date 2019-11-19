package sfoster3.Mnswpr.Game

import sfoster3.Mnswpr.MineField.{Coordinate, MineField}

object SafeBufferGenerator extends MineFieldGenerator {
  def generateMineField(start: Coordinate, width: Int, height: Int, count: Int, seed: Option[Int] = None): MineField = {
    val rand = getRandomSeed(seed)
    validateCount(width, height, count)
    val safeZone = for {
      dx <- -1 to 1
      dy <- -1 to 1
    } yield Coordinate(start.x + dx, start.y + dy)
    val mines = Iterator.continually {
      Coordinate(rand.between(0, width - 1), rand.between(0, height - 1))
    }.filter(!safeZone.contains(_)).distinct.take(count).toSet
    MineField(width, height, mines)
  }
}
