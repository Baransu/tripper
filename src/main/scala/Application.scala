import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal

object Application {
  type Journal = LeveldbReadJournal
}
