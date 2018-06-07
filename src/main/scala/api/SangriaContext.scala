package api

import domain.UserCommands.CreateUser
import domain.{User, UserQueryRepository, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

case class SangriaContext(userRepository: UserRepository,
                          userQueryReository: UserQueryRepository
                         )
                         (implicit ec: ExecutionContext) extends Mutation {

  // queries
  def getUsers: (Int, Int) => Future[List[User]] = userQueryReository.getUsers

  def getUserDetails: String => Future[Option[User]] = userRepository.getUserDetails

  // mutations

  def createUser: CreateUser => Future[User] = userRepository.createUser
}