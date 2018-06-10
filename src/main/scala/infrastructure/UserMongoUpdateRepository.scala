package infrastructure

import java.util.concurrent.atomic.AtomicBoolean

import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.{EventEnvelope, Offset}
import akka.stream.ActorMaterializer
import domain.UserEvent.UserCreated
import infrastructure.MongoWRs.UserDTO
import infrastructure.generic.MongoUpdateRepository
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

class UserMongoUpdateRepository(readJournal: LeveldbReadJournal,
                                collection: BSONCollection,
                                databaseReady: AtomicBoolean
                               )(implicit ec: ExecutionContext, mat: ActorMaterializer)
  extends MongoUpdateRepository(readJournal, databaseReady, "UserMongoUpdateRepository") {

  override protected def persistEvents(event: EventEnvelope): Future[Offset] =
    handleEvent(event.event).map(_ => event.offset)

  def handleEvent: PartialFunction[Any, Future[Any]] = {
    case UserCreated(id, name, email, _) => {
      println("Persisting UserCreated")
      collection
        .update(
          BSONDocument("_id" -> id),
          UserDTO(id, name, email),
          upsert = true)
    }
  }

  //  private def setUserFields(id: String, updates: BSONDocument) =
  //    updateUser(id, BSONDocument("$set" → updates))

  //  private def updateUser(id: String, query: BSONDocument) =
  //    collection.update(BSONDocument("_id" → id), query)

}
