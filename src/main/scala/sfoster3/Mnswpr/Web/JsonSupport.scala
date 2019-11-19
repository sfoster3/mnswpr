package sfoster3.Mnswpr.Web

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import sfoster3.Mnswpr.Game.GameMessages.{GameCreated, VisibleBoard}
import sfoster3.Mnswpr.Game.GameSession._
import sfoster3.Mnswpr.MineField.Coordinate
import spray.json.{JsNumber, JsString, JsValue, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)

  import spray.json.DefaultJsonProtocol._

  implicit val coordinateJsonFormat: RootJsonFormat[Coordinate] = jsonFormat2(Coordinate)
  implicit val gameStartArgsJsonFormat: RootJsonFormat[GameStartArgs] = jsonFormat3(GameStartArgs)

  implicit object CellJsonFormat extends RootJsonFormat[Cell] {
    override def write(cell: Cell): JsValue = cell match {
      case UnknownCell() => JsString("?")
      case FlaggedCell() => JsString("F")
      case RevealedCell(n) => JsNumber(n)
      case MineCell() => JsString("X")
    }

    override def read(json: JsValue): Cell = json match {
      case JsString("?") => UnknownCell()
      case JsString("F") => FlaggedCell()
      case JsNumber(n) => RevealedCell(n.toInt)
      case JsString("X") => MineCell()
      case _ => UnknownCell()
    }
  }

  implicit val visibleBoardJsonFormat: RootJsonFormat[VisibleBoard] = jsonFormat5(VisibleBoard)
  implicit val apiActionResponseJsonFormat: RootJsonFormat[APIActionResponse] = jsonFormat2(APIActionResponse)
  implicit val gameCreatedJsonFormat: RootJsonFormat[GameCreated] = jsonFormat1(GameCreated)
}
