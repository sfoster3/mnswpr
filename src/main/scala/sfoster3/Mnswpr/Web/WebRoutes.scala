package sfoster3.Mnswpr.Web

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.PathDirectives.pathPrefix
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.pattern.ask
import akka.util.Timeout
import sfoster3.Mnswpr.Actor.Errors.NotFoundException
import sfoster3.Mnswpr.Game.GameMessages._
import sfoster3.Mnswpr.MineField.Coordinate

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

sealed case class GameStartArgs(width: Int, height: Int, count: Int)

trait WebRoutes extends JsonSupport {

  val gameBroker: ActorRef
  implicit val timeout: Timeout
  implicit lazy val duration: Duration = timeout.duration
  implicit val executionContext: ExecutionContext

  implicit def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: NotFoundException => complete(404, e.getMessage)
    case e: Exception =>
      e.printStackTrace()
      complete(500, s"DEBUG; ${e.getMessage}")
    case e => complete(500, s"Something went wrong")
  }

  private def askBroker(gameId: Int, message: GameMessage): Route =
    handleGameResult((gameBroker ? BrokerMessage(gameId, message)).mapTo[GameResult])

  private def handleGameResult(fut: Future[GameResult]): Route = Await.result(fut, duration) match {
    case board: VisibleBoard => complete(board)
    case VisibleLoss(board, mines) =>
      gameBroker ? DeleteGame(board.gameId)
      complete("You Lose")
  }

  private def actionRoute(routeName: String, getMessage: Function[Coordinate, GameMessage])(gameId: Int): Route =
    path(routeName) {
      pathEnd {
        post {
          entity(as[Coordinate]) {
            coordinate => askBroker(gameId, getMessage(coordinate))
          }
        }
      }
    }

  lazy val webRoutes: Route =
    concat(
      // Static files
      pathSingleSlash {
        pathEnd {
          get {
            getFromFile("src/main/dist/index.html")
          }
        }
      },
      get {
        getFromDirectory("src/main/dist")
      },

      // Api
      pathPrefix("api") {
        concat(
          pathPrefix("v1") {
            concat(
              pathPrefix("board") {
                concat(
                  pathEnd {
                    post {
                      entity(as[GameStartArgs]) {
                        case GameStartArgs(width, height, count) =>
                          val gameCreated: GameCreated = Await.result((gameBroker ? CreateGame(width, height, count)).mapTo[GameCreated], duration)
                          complete(gameCreated)
                      }
                    }
                  },
                  pathPrefix(IntNumber) {
                    gameId =>
                      concat(
                        // GET
                        pathEnd {
                          get {
                            askBroker(gameId, GetVisible())
                          }
                        },

                        // POST action routes taking a coordinate
                        actionRoute("reveal", Reveal)(gameId),
                        actionRoute("flag", Flag)(gameId),
                        actionRoute("revealAdj", RevealAdj)(gameId)
                      )
                  }
                )
              }
            )
          }
        )
      }
    )
}
