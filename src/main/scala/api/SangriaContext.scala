package api

import domain.UserCommands.CreateUser
import domain.{User, UserRepository}
import infrastructure.{Character, CharacterRepo, Droid, Episode, Human}

import scala.concurrent.{ExecutionContext, Future}

case class SangriaContext(characterRepo: CharacterRepo,
                          userRepository: UserRepository)
                         (implicit ec: ExecutionContext) extends Mutation {

  def getHumans: (Int, Int) => List[Human] = characterRepo.getHumans

  def getDroids: (Int, Int) => List[Droid] = characterRepo.getDroids

  def getHero: Option[Episode.Value] => Character = characterRepo.getHero

  def getHuman: String => Option[Human] = characterRepo.getHuman

  def getDroid: String => Option[Droid] = characterRepo.getDroid

  def getUserDetails: String => Future[Option[User]] = userRepository.getUserDetails

  def createUser: CreateUser => Future[User] = userRepository.createUser
}