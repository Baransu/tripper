package domain

import java.time.ZonedDateTime

sealed trait UserEvent

object UserEvent {

  final case class UserCreated(id: String, name: String, email: String, time: ZonedDateTime) extends UserEvent

  //  final case class DetailsUpdated(id: String, name: String, email: String, time: ZonedDateTime) extends UserEvent
}

