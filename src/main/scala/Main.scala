import java.util.concurrent.atomic.AtomicBoolean

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.persistence.query.{EventEnvelope, Offset}
import akka.persistence.query.scaladsl.{CurrentEventsByTagQuery, EventsByTagQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import api.{GraphQLEndpoint, GraphQLSchema, SangriaContext}
import com.typesafe.config.ConfigFactory
import infrastructure.{InMemUserRepository, UserMongoQueryRepository, UserMongoUpdateRepository}
import reactivemongo.api.{MongoConnection, MongoDriver}
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}

object Main extends App {
  private val config = ConfigFactory.load("default.conf")

  implicit val system = ActorSystem("tripper-actor-system", config)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val mongoHost = config.getString("mongo.host")
  private val mongoPort = config.getInt("mongo.port")
  private val databaseName = config.getString("mongo.db.name")

  private val driver = MongoDriver()
  private val mongoUri = s"mongodb://$mongoHost:$mongoPort/$databaseName"
  private val connection = MongoConnection.parseURI(mongoUri).map(driver.connection)
  private val db = Await.result(Future.fromTry(connection).flatMap(_.database(databaseName)), 30 seconds)

  private val readJournal = new EventsByTagQuery with CurrentEventsByTagQuery {
    override def eventsByTag(tag: String, offset: Offset): Source[EventEnvelope, NotUsed] = Source.empty

    override def currentEventsByTag(tag: String, offset: Offset): Source[EventEnvelope, NotUsed] = Source.empty
  }

  val databaseReady = new AtomicBoolean(false)
  val collection = db.collection[BSONCollection]("users")

  private val userUpdateRepository = new UserMongoUpdateRepository(readJournal, collection, databaseReady)
  private val userQueryRepository = new UserMongoQueryRepository(collection, databaseReady)

  private val userRepository = new InMemUserRepository()

  private val sangriaContext = new SangriaContext(userRepository, userQueryRepository)
  private val graphQLSchema = new GraphQLSchema()
  private val graphQLEndpoint = new GraphQLEndpoint(graphQLSchema, sangriaContext)

  // TODO: get from configuration
  val hostToBind = "localhost"
  val portToBind = 8080

  userUpdateRepository.readEvents {
    try {
      Http().bindAndHandle(graphQLEndpoint.route, hostToBind, portToBind)
        .onComplete {
          case Success(_) => println(s"API started at $hostToBind:$portToBind")
          case Failure(ex) => println(s"Cannot bind API to $hostToBind:$portToBind", ex)
        }
    } catch {
      case ex: Throwable => println("Error encountered while binding UserQueryServiceSystem", ex)
    }
  }


}