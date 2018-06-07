package infrastructure

import java.util.concurrent.atomic.AtomicBoolean

import domain.{User, UserQueryRepository}
import infrastructure.MongoWRs.UserDTO
import infrastructure.generic.MongoQueryRepository
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

class UserMongoQueryRepository(collection: BSONCollection, databaseReady: AtomicBoolean)(implicit ec: ExecutionContext)
  extends MongoQueryRepository[UserDTO](collection, databaseReady) with UserQueryRepository {

  override def getUsers(limit: Int, offset: Int): Future[List[User]] =
    query(limit, offset).map(result => result.elements.map(_.toDomain))

}
