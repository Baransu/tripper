import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import api.{GraphQLEndpoint, GraphQLSchema, SangriaContext}
import com.typesafe.config.ConfigFactory
import infrastructure.{CharacterRepo, InMemUserRepository}

import scala.util.{Failure, Success}

object Main extends App {
  private val config = ConfigFactory.load("default.conf")

  implicit val system = ActorSystem("tripper-actor-system", config)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val userRepository = new InMemUserRepository()
  private val characterRepo = new CharacterRepo()

  private val sangriaContext = new SangriaContext(characterRepo, userRepository)
  private val graphQLSchema = new GraphQLSchema()
  private val graphQLEndpoint = new GraphQLEndpoint(graphQLSchema, sangriaContext)

  val hostToBind = "localhost"
  val portToBind = 8080

  Http().bindAndHandle(graphQLEndpoint.route, hostToBind, portToBind)
    .onComplete {
      case Success(_) => println(s"API started at $hostToBind:$portToBind")
      case Failure(ex) => println(s"Cannot bind API to $hostToBind:$portToBind", ex)
    }

}