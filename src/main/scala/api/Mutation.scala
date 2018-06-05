package api

import domain.User
import domain.UserCommands.CreateUser
import sangria.macros.derive.GraphQLField

import scala.concurrent.Future

trait Mutation {
  this: SangriaContext =>

  @GraphQLField
  def createUser(name: String, email: String): Future[Option[User]] =
    createUser(CreateUser(name, email))
}