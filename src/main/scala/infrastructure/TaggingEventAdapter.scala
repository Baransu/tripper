package infrastructure

import akka.persistence.journal.{Tagged, WriteEventAdapter}

class TaggingEventAdapter extends WriteEventAdapter {

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = {
    println(s"Event adapter is sending ${event.getClass.getName} to serializer as passthrough")
    val tags = Set("all")
    Tagged(event, tags)
  }
}