package api

import domain.User
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.macros.derive._
import sangria.schema._

import scala.concurrent.{ExecutionContext, Future}

class GraphQLSchema()(implicit ex: ExecutionContext) {

  implicit val UserType =
    ObjectType(
      "User",
      "...",
      fields[SangriaContext, User](
        Field("id", IDType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field("email", StringType, resolve = _.value.email)
      )
    )

  val ID = Argument("id", IDType)

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 10)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)

  val Query = ObjectType(
    "Query", fields[SangriaContext, Unit](
      Field("user", OptionType(UserType),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getUserDetails(ctx arg ID)
      ),
      Field("users", ListType(UserType),
        arguments = LimitArg :: OffsetArg :: Nil,
        resolve = ctx => ctx.ctx.getUsers(ctx arg LimitArg, ctx arg OffsetArg)
      )
    ))

  val MutationType = deriveContextObjectType[SangriaContext, Mutation, Unit](identity)

  val StarWarsSchema = Schema(Query, Some(MutationType))
}