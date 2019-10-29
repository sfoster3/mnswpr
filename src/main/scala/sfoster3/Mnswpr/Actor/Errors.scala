package sfoster3.Mnswpr.Actor

object Errors {

  class NotFoundException(message: String) extends Exception {
    override def getMessage: String = message
  }

}
