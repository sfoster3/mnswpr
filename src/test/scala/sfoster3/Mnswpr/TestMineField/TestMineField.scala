package sfoster3.Mnswpr.TestMineField

import org.scalatest._
import sfoster3.Mnswpr.MineField.Conversions._
import sfoster3.Mnswpr.MineField.MineField

import scala.language.implicitConversions

class TestMineField extends WordSpec with Matchers {

  "A MineField" must {
    "be creatable from a set" in {
      val mines = Set((1, 2), (4, 6), (9, 0))
      MineField(10, 10, mines)
    }

    "allow checking for mines" in {
      val field = MineField(4, 4, Set(0 -> 1, 1 -> 2, 2 -> 3))
      assert(field.isMine(0 -> 1))
      assert(field.isMine(1 -> 2))
      assert(field.isMine(2 -> 3))
      assert(!field.isMine(1 -> 1))
      assert(!field.isMine(2 -> 1))
    }

    "allow checking for bounds" in {
      val field = MineField(4, 6, Set())
      assert(field inBounds (0 -> 0))
      assert(field inBounds (3 -> 5))
      assert(!field.inBounds(4 -> 5))
      assert(!field.inBounds(-1 -> 0))
    }

    "pretty print" in {
      val field = MineField(4, 4, Set(0 -> 0, 0 -> 1, 1 -> 0, 1 -> 2, 1 -> 3, 2 -> 2, 3 -> 0, 3 -> 3))
      assert("x x - x\nx - - -\n- x x -\n- x - x" == field.prettyPrint)
    }

    "get numbers for cells" in {
      /*  x x - x   x x 2 x
        x - - -   x 4 2 1
        - x - -   3 x 3 1
        - x - x   2 x 3 x */
      val field = MineField(4, 4, Set(0 -> 0, 0 -> 1, 1 -> 0, 1 -> 2, 1 -> 3, 3 -> 0, 3 -> 3))
      assert(field.getNum(0 -> 0).isEmpty, "Mine 0,0 should have no count")

      List(
        (2 -> 0, 2),
        (1 -> 1, 4),
        (2 -> 1, 3),
        (3 -> 2, 1),
        (0 -> 2, 3),
        (2 -> 2, 3),
        (3 -> 2, 1),
        (0 -> 3, 2),
        (2 -> 3, 3)
      ).foreach {
        case ((x, y), z) => assert(field.getNum(x -> y).contains(z), s"Mine $x,$y should have count $z")
      }
    }
  }
}
