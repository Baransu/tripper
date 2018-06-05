package domain

import domain.UserCommands.CreateUser

import scala.concurrent.Future

trait UserRepository {
  def getUserDetails(id: String): Future[Option[User]]

  def createUser(createCommand: CreateUser): Future[User]
}
