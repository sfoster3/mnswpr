package sfoster3.Mnswpr.Web

import sfoster3.Mnswpr.Game.GameMessages.VisibleBoard

case class APIActionResponse(board: VisibleBoard, isLoss: Boolean = false)

