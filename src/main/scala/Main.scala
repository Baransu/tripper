import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.util.{Failure, Success}
import infrastructure.{CharacterRepo, InMemUserRepository}
import api.{GraphQLEndpoint, GraphQLSchema, SangriaContext}

object Main {
  implicit val system = ActorSystem("tripper-actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val userRepository = new InMemUserRepository()
  private val characterRepo = new CharacterRepo()

  private val sangriaContext = new SangriaContext(characterRepo, userRepository)
  private val graphQLSchema = new GraphQLSchema()
  private val graphQLEndpoint = new GraphQLEndpoint(graphQLSchema, sangriaContext)

  def main(args: Array[String]) {

    val hostToBind = "localhost"
    val portToBind = 8080

    Http().bindAndHandle(graphQLEndpoint.route, hostToBind, portToBind)
      .onComplete {
        case Success(_) => println(s"API started at $hostToBind:$portToBind")
        case Failure(ex) => println(s"Cannot bind API to $hostToBind:$portToBind", ex)
      }
  }
}