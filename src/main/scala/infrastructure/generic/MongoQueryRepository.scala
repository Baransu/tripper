package infrastructure.generic

import java.util.concurrent.atomic.AtomicBoolean

import reactivemongo.api.BSONSerializationPack.Reader
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

object MongoQueryRepository {

  final case class QueryResult[DTO](elements: List[DTO])

  final case class FilterRange(limit: Int, offset: Int)

}

abstract class MongoQueryRepository[DTO](collection: BSONCollection, databaseReady: AtomicBoolean)(
  implicit ec: ExecutionContext) {

  import MongoQueryRepository._

  protected def query(limit: Int, offset: Int)(implicit reader: Reader[DTO]): Future[QueryResult[DTO]] = {
    if (!databaseReady.get) throw new IllegalStateException("Database is not ready/stream failed")

    for {
      elements <- collection
        .find(BSONDocument())
        .options(QueryOpts(offset, limit))
        .cursor[DTO]()
        .collect[List](limit, Cursor.FailOnError[List[DTO]]())
    } yield QueryResult(elements)
  }
}
