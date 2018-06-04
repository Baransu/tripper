package domain

import java.time.ZonedDateTime

import akka.persistence.{PersistentActor, RecoveryCompleted}
import domain.UserCommands.{CreateUser, Get}
import domain.UserEvent.{DetailsAssigned, UserCreated}
import scala.collection.immutable.Seq

import scala.concurrent.ExecutionContext

class UserActor(id: String)(implicit ec: ExecutionContext) extends PersistentActor {
  import UserActor._

  override def persistenceId: String = id

  var state: UserActorState = UserActorState.empty

  override def receiveCommand: Receive = {
    case CreateUser(name, email) =>
      persistAll(
        Seq(
          UserCreated(id, ZonedDateTime.now),
          DetailsAssigned(name, email, ZonedDateTime.now)
        )
      )(handleEvent)
      deferAsync(()) { _ ⇒
        sender ! id
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
    case UserCreated(_, _) =>
      context.become(initialized)

    case DetailsAssigned(name, email, _) => {
      state = state.copy(
        Some(User(name, email))
      )
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
