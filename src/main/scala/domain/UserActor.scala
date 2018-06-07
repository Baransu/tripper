package domain

import java.time.ZonedDateTime

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted}
import domain.UserCommands.{CreateUser, Get}
import domain.UserEvent.{UserCreated}

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext

class UserActor(id: String)(implicit ec: ExecutionContext) extends PersistentActor with ActorLogging {
  import UserActor._

  override def persistenceId: String = id

  var state: UserActorState = UserActorState.empty

  override def receiveCommand: Receive = {
    case CreateUser(name, email) =>
      persistAll(
        Seq(UserCreated(id, name, email, ZonedDateTime.now))
      )(handleEvent)
      deferAsync(()) { _ ⇒
        sender ! User(id, name, email)
      }

    case _ => sender ! None
  }

  private def initialized: Receive = {
    case Get => sender ! state.user
  }

  override def receiveRecover: Receive = {
    case e: UserEvent      => handleEvent(e)
    case RecoveryCompleted => // ignore
  }

  private def handleEvent: PartialFunction[UserEvent, Unit] = {
    case UserCreated(id, name, email, _) => {
      state = state.copy(
        Some(User(id, name, email))
      )
      context.become(initialized)
    }
  }

}

object UserActor {
  final case class UserActorState(user: Option[User])

  object UserActorState {
    def empty = UserActorState(
      user = None,
    )
  }

}
