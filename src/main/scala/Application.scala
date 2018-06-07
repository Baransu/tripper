import akka.persistence.query.scaladsl.{CurrentEventsByTagQuery, EventsByTagQuery}

object Application {
  type Journal = CurrentEventsByTagQuery with EventsByTagQuery
}
