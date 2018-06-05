package api

import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, OK}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, _}
import akka.http.scaladsl.server.{Route, StandardRoute}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, JObject, JString, JValue, Serialization}
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.json4s.native._
import sangria.parser.QueryParser

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class GraphQLEndpoint(graphQLSchema: GraphQLSchema, sangriaContext: SangriaContext)
                     (implicit ec: ExecutionContext) extends Json4sSupport {

  protected implicit val formats: Formats             = DefaultFormats
  protected implicit val serialization: Serialization = Serialization

  def executeQuery(query: String, variables: JValue, operationName: Option[String]): StandardRoute =
    QueryParser.parse(query) match {
      case Success(queryAst) =>
        complete (
          Executor
            .execute(
              graphQLSchema.StarWarsSchema,
              queryAst,
              sangriaContext,
              operationName = operationName,
              variables = variables)
            .map (OK → _)
            .recover {
              case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
              case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
            }
        )
      case Failure(_) => complete(BadRequest -> Map("message" -> "Cannot parse query"))
    }


  val route: Route =
    (path("graphql") & post & entity(as[JObject])) { (body) =>
      (body \ "query").extractOpt[String] match {
        case Some(query) =>
          val variables = (body \ "variables").extractOpt[JObject].getOrElse(JObject())
          val operationName = (body \ "operationName").extractOpt[String]
          executeQuery(query, variables, operationName)

        case None => complete(BadRequest)
      }
    }

}
