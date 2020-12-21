package sfoster3.Mnswpr.Web

import sfoster3.Mnswpr.Game.GameMessages.VisibleBoard
import sfoster3.Mnswpr.Web.ResultType.ResultType

object ResultType extends Enumeration {
  type ResultType = Value
  val Win, Loss, None = Value
}

case class APIActionResponse(
    board: VisibleBoard,
    result: ResultType = ResultType.None
)
