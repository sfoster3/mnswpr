package sfoster3.Mnswpr.Web

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import sfoster3.Mnswpr.Game.GameBroker

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

object WebServer extends App with WebRoutes {

  implicit val system: ActorSystem = ActorSystem("MnswprHttpServer")
  implicit val timeout: Timeout = Duration.create(5, TimeUnit.SECONDS)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val gameBroker: ActorRef = system.actorOf(GameBroker.props, "gameBrokerActor")

  lazy val routes = webRoutes

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Running at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Oh no!")
      e.printStackTrace()
      system.terminate()
  }

  // Wait For "ENTER"
  StdIn.readLine()
  // Unbind from the port and shut down when done
  serverBinding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

  Await.result(system.whenTerminated, Duration.Inf)
}