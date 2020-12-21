package sfoster3.Mnswpr.MineField

import scala.collection.mutable
import scala.language.implicitConversions

sealed case class Coordinate(x: Int, y: Int) {
  def getAdj: Set[Coordinate] =
    (for {
      dx <- -1 to 1
      dy <- -1 to 1
    } yield Coordinate(x + dx, y + dy)).toSet
}
object CoordinateConverters {
  implicit def convertTuple(tuple: (Int, Int)): Coordinate =
    Coordinate(tuple._1, tuple._2)
}

sealed case class MineField(
    width: Int,
    height: Int,
    mines: Map[Coordinate, Boolean]
) {

  private val _numCache: mutable.Map[Coordinate, Option[Int]] = mutable.Map()

  def getNum(c: Coordinate): Option[Int] =
    _numCache.getOrElseUpdate(
      c,
      if (isMine(c)) None else Some(getAdj(c) count isMine)
    )

  def getAdj(c: Coordinate): Set[Coordinate] =
    c.getAdj.filter {
      case Coordinate(c.x, c.y) => false
      case co                   => inBounds(co)
    }

  def inBounds(c: Coordinate): Boolean =
    c.x >= 0 && c.x < width && c.y >= 0 && c.y < height

  def prettyPrint: String =
    (0 until height)
      .map(y =>
        (0 until width)
          .map { x => if (isMine(Coordinate(x, y))) "x" else "-" }
          .reduce(_ + " " + _)
      )
      .reduce(_ + "\n" + _)

  def isMine(c: Coordinate): Boolean = mines.getOrElse(c, false)

  override def toString: String =
    mines.map { case Coordinate(x, y) -> _ => x -> y }.toSet.toString

}

object MineField {
  def apply[A](width: Int, height: Int, mines: Set[A])(implicit
      converter: A => Coordinate
  ): MineField =
    MineField(width, height, mines.map { a => a -> true }.toMap)

  def apply[A](width: Int, height: Int, mines: Map[A, Boolean])(implicit
      converter: A => Coordinate
  ): MineField =
    new MineField(width, height, mines map { case a -> b => converter(a) -> b })
}
