package domain

sealed trait UserCommand

object UserCommands {
  final case class CreateUser(name: String, email: String) extends UserCommand

  case object Get extends UserCommand
}
