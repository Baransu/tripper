package api

import domain.User
import infrastructure.{Character, Droid, Episode, Human}
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.macros.derive.deriveContextObjectType
import sangria.schema._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Defines a GraphQL schema for the current project
  */
class GraphQLSchema()(implicit ex: ExecutionContext) {
  /**
    * Resolves the lists of characters. These resolutions are batched and
    * cached for the duration of a query.
    */
  val characters = Fetcher.caching(
    (ctx: SangriaContext, ids: Seq[String]) =>
      Future.successful(ids.flatMap(id => ctx.getHuman(id).orElse(ctx.getDroid(id)))))(HasId(_.id))

  val EpisodeEnum = EnumType(
    "Episode",
    Some("One of the films in the Star Wars Trilogy"),
    List(
      EnumValue("NEWHOPE",
        value = Episode.NEWHOPE,
        description = Some("Released in 1977.")),

      EnumValue("EMPIRE",
        value = Episode.EMPIRE,
        description = Some("Released in 1980.")),

      EnumValue("JEDI",
        value = Episode.JEDI,
        description = Some("Released in 1983.")))
    )

  val Character: InterfaceType[SangriaContext, Character] =
    InterfaceType(
      "Character",
      "A character in the Star Wars Trilogy",
      () => fields[SangriaContext, Character](
        Field("id", StringType,
          Some("The id of the character."),
          resolve = _.value.id),

        Field("name", OptionType(StringType),
          Some("The name of the character."),
          resolve = _.value.name),

        Field("friends", ListType(Character),
          Some("The friends of the character, or an empty list if they have none."),
          resolve = ctx => characters.deferSeqOpt(ctx.value.friends)),

        Field("appearsIn", OptionType(ListType(OptionType(EpisodeEnum))),
          Some("Which movies they appear in."),
          resolve = _.value.appearsIn map (e => Some(e)))
      ))

  val UserType =
    ObjectType(
      "User",
      "...",
      fields[SangriaContext, User](
        Field("id", IDType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field("email", StringType, resolve = _.value.email)
      )
    )

  val Human =
    ObjectType(
      "Human",
      "A humanoid creature in the Star Wars universe.",
      interfaces[SangriaContext, Human](Character),
      fields[SangriaContext, Human](
        Field("id", StringType,
          Some("The id of the human."),
          resolve = _.value.id),

        Field("name", OptionType(StringType),
          Some("The name of the human."),
          resolve = _.value.name),

        Field("friends", ListType(Character),
          Some("The friends of the human, or an empty list if they have none."),
          resolve = ctx => characters.deferSeqOpt(ctx.value.friends)),

        Field("appearsIn", OptionType(ListType(OptionType(EpisodeEnum))),
          Some("Which movies they appear in."),
          resolve = _.value.appearsIn map (e => Some(e))),

        Field("homePlanet", OptionType(StringType),
          Some("The home planet of the human, or null if unknown."),
          resolve = _.value.homePlanet)
      ))

  val Droid = ObjectType(
    "Droid",
    "A mechanical creature in the Star Wars universe.",
    interfaces[SangriaContext, Droid](Character),
    fields[SangriaContext, Droid](
      Field("id", StringType,
        Some("The id of the droid."),
        resolve = _.value.id),

      Field("name", OptionType(StringType),
        Some("The name of the droid."),
        resolve = ctx => Future.successful(ctx.value.name)),

      Field("friends", ListType(Character),
        Some("The friends of the droid, or an empty list if they have none."),
        resolve = ctx => characters.deferSeqOpt(ctx.value.friends)),

      Field("appearsIn", OptionType(ListType(OptionType(EpisodeEnum))),
        Some("Which movies they appear in."),
        resolve = _.value.appearsIn map (e => Some(e))),

      Field("primaryFunction", OptionType(StringType),
        Some("The primary function of the droid."),
        resolve = _.value.primaryFunction)
    ))

  val ID = Argument("id", StringType, description = "id of the character")

  val EpisodeArg = Argument("episode", OptionInputType(EpisodeEnum),
    description = "If omitted, returns the hero of the whole saga. If provided, returns the hero of that particular episode.")

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 20)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)

  val Query = ObjectType(
    "Query", fields[SangriaContext, Unit](
      Field("user", OptionType(UserType),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getUserDetails(ctx arg ID)
      ),

      Field("hero", Character,
        arguments = EpisodeArg :: Nil,
        deprecationReason = Some("Use `human` or `droid` fields instead"),
        resolve = (ctx) => ctx.ctx.getHero(ctx.arg(EpisodeArg))),

      Field("human", OptionType(Human),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getHuman(ctx arg ID)),

      Field("droid", Droid,
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getDroid(ctx arg ID).get),

      Field("humans", ListType(Human),
        arguments = LimitArg :: OffsetArg :: Nil,
        resolve = ctx => ctx.ctx.getHumans(ctx arg LimitArg, ctx arg OffsetArg)),

      Field("droids", ListType(Droid),
        arguments = LimitArg :: OffsetArg :: Nil,
        resolve = ctx => ctx.ctx.getDroids(ctx arg LimitArg, ctx arg OffsetArg))
    ))

  val MutationType = deriveContextObjectType[SangriaContext, Mutation, Unit](identity)

  val StarWarsSchema = Schema(Query, Some(MutationType))
}