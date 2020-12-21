package sfoster3.Mnswpr.MineField

import scala.language.implicitConversions

object Conversions {
  implicit def toCoordinate(tu: (Int, Int)): Coordinate =
    Coordinate(tu._1, tu._2)
}
