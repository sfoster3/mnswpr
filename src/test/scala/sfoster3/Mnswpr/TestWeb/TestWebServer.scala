package sfoster3.Mnswpr.TestWeb

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import sfoster3.Mnswpr.Web.WebServer

class TestWebServer extends WordSpec with Matchers with ScalatestRouteTest {

  "The WebServer" must {
    "serve the compiled static files" in {
      Get("/src/main/static/placeholder.txt") ~> WebServer.webRoutes ~> check {
        responseAs[String]
      }
    }
  }

}
