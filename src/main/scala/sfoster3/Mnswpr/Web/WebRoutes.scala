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

sealed case class GameStartArgs(width: Int, height: Int, count: Int, start: Coordinate)

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
    case VisibleResult(board) => complete(board)
    case VisibleLoss(board, mines) => complete("You Lose")
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
            getFromResource("dist/index.html")
          }
        }
      },
      get {
        getFromResourceDirectory("dist")
      },

      //
      pathPrefix("static") {
        getFromResourceDirectory("dist")
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
                        case GameStartArgs(width, height, count, start) =>
                          askBroker(
                            Await.result((gameBroker ? CreateGame(width, height, count, start))
                              .mapTo[GameCreated].map {
                              case GameCreated(gameId) => gameId
                            }, duration), Reveal(start))
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
