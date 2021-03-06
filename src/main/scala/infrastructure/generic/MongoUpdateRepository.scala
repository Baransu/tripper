package infrastructure.generic

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

import akka.NotUsed
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal

import scala.util.{Failure, Success}
import akka.persistence.query._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

abstract class MongoUpdateRepository(readJournal: LeveldbReadJournal,
                                     databaseReady: AtomicBoolean,
                                     name: String)
                                    (implicit ec: ExecutionContext, mat: ActorMaterializer) {

  private val processingOffsetList = new ConcurrentLinkedQueue[Offset]()
  private var lastProcessedOffset = Offset.noOffset

  def readEvents(callback: => Unit) = {
    readJournal
      .currentEventsByTag("all", Offset.noOffset)
      .via(persistFlow)
      .map(offset => lastProcessedOffset = offset)
      .runWith(Sink.ignore)
      .onComplete {
        case Success(_) => {
          println("Old events stream finished. Starting new event stream")
          databaseReady.set(true)
          readNewEvents(getStartOffset)
          callback
        }
        case Failure(ex) => {
          println(s"CurrentEvents stream failed in $name", ex)
          databaseReady.set(false)
        }
      }
  }

  private def readNewEvents(lastOffset: Offset, delay: FiniteDuration = 0 seconds) = {
    readJournal
      .eventsByTag("all", lastOffset)
      .initialDelay(delay)
      .via(persistFlow)
      .runWith(Sink.foreach(_ => databaseReady.set(true)))
      .onComplete {
        case Success(_) => handleStreamStopped()
        case Failure(ex) =>
          println(s"Exception while running a stream in $name", ex)
          handleStreamStopped()
      }
  }

  private def handleStreamStopped(): Unit = {
    databaseReady.set(false)
    println(s"Stream stopped in $name, restarting")
    readNewEvents(getStartOffset, 5 seconds)
  }

  private def getStartOffset =
    if (!processingOffsetList.isEmpty) {
      processingOffsetList.asScala.toList.min((x: Offset, y: Offset) =>
        (x, y) match {
          case (fst: Sequence, snd: Sequence) => fst.compare(snd)
          case _ => throw new IllegalStateException("Incorrect offset format")
        })
    } else lastProcessedOffset

  private def persistFlow: Flow[EventEnvelope, Offset, NotUsed] =
    preProcessingFlow
      .via(processingFlow)
      .via(postProcessingFlow)

  private def preProcessingFlow: Flow[EventEnvelope, EventEnvelope, NotUsed] =
    Flow[EventEnvelope].map { event =>
      processingOffsetList.add(event.offset)
      event
    }

  private def processingFlow: Flow[EventEnvelope, Offset, NotUsed] =
    Flow[EventEnvelope].mapAsync(1)(persistEvents)

  private def postProcessingFlow: Flow[Offset, Offset, NotUsed] = Flow[Offset].map { offset =>
    processingOffsetList remove offset
    (offset, lastProcessedOffset) match {
      case (fst: Sequence, NoOffset) => lastProcessedOffset = fst
      case (fst: Sequence, snd: Sequence) => if (fst.compare(snd) > 0) lastProcessedOffset = offset
      case (fst, snd) =>
        throw new IllegalStateException(s"Incorrect offset format: first: $fst, second: $snd")
    }
    offset
  }

  protected def persistEvents(event: EventEnvelope): Future[Offset]

}
