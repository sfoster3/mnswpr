package sfoster3.Mnswpr.Game

import java.security.SecureRandom

import sfoster3.Mnswpr.MineField.{Coordinate, MineField}

import scala.util.Random

trait MineFieldGenerator {
  def generateMineField(start: Coordinate, width: Int, height: Int, count: Int, seed: Option[Int] = None): MineField = {
    validateCount(width, height, count)
    doGeneration(start, width, height, count, getRandomSeed(seed))
  }

  protected def doGeneration(start: Coordinate, width: Int, height: Int, count: Int, random: Random): MineField

  protected def validateCount(width: Int, height: Int, count: Int): Unit = {
    if (count > ((width * height) / 2)) {
      throw new Exception(s"$count is too many mines for a board of ${width}x$height")
    }
  }

  protected def getRandomSeed(seed: Option[Int]): Random = {
    val randSeed: Array[Byte] = seed.map(BigInt(_).toByteArray).getOrElse(SecureRandom.getSeed(100))
    new Random(BigInt(randSeed).toLong)
  }
}