package infrastructure

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import domain.UserCommands._
import domain.{User, UserActor, UserCommand, UserRepository}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class InMemUserRepository()(implicit system: ActorSystem, ec: ExecutionContext) extends UserRepository {

  import InMemUserRepository._

  private implicit val timeout: Timeout = Timeout(20 seconds)

  private val proxyActor: ActorRef = system.actorOf(Props(new ProxyActor))

  class ProxyActor extends Actor {
    override def receive: Receive = {
      case ProxyRequest(id, command) =>
        context
          .child(id)
          .getOrElse(context.actorOf(Props(new UserActor(id)), id))
          .forward(command)
    }
  }

  private def command[Result: ClassTag](id: String, command: UserCommand): Future[Result] =
    (proxyActor ? ProxyRequest(id, command)).mapTo[Result]

  override def getUserDetails(id: String): Future[Option[User]] =
    command[Option[User]](id, Get)

  override def createUser(createCommand: CreateUser): Future[User] = {
    val id = UUID.randomUUID().toString
    command[User](id, createCommand)
  }
}

object InMemUserRepository {
  private final case class ProxyRequest(id: String, command: UserCommand)
}