package domain

import java.time.ZonedDateTime

sealed trait UserEvent

object UserEvent {
  final case class UserCreated(id: String, time: ZonedDateTime) extends UserEvent

  final case class DetailsAssigned(name: String, email: String, time: ZonedDateTime) extends UserEvent
}

