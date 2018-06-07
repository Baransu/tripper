package infrastructure

import domain.User
import reactivemongo.bson.{BSONDocumentHandler, Macros}
import reactivemongo.bson.Macros.Annotations.Key

object MongoWRs {
  implicit val transferHandler: BSONDocumentHandler[UserDTO] = Macros.handler[UserDTO]

  final case class UserDTO(
                            @Key("id") id: String,
                            name: String,
                            email: String
                          ) {
    def toDomain: User = User(id, name, email)
  }

}
