package domain

import scala.concurrent.Future

trait UserQueryRepository {
  def getUsers(limit: Int, offset: Int): Future[List[User]]
}
