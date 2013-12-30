package config

import scala.collection.JavaConverters._

import play.api.Play
import play.api.Play.current

object Config {
  private val configuration = Play.configuration

  lazy val databaseServers: Option[java.util.List[java.lang.String]] = configuration.getStringList("mongodb.servers")

  lazy val databaseServersSeq: Option[Seq[String]] = databaseServers.fold[Option[Seq[String]]](None){list => Some(list.asScala)}

  lazy val databaseName: Option[java.lang.String] = configuration.getString("mongodb.database.name")

  lazy val mailboxCollectionName: Option[java.lang.String] = configuration.getString("mongodb.collections.mailboxes.name")

  lazy val messagesCollectionName: Option[java.lang.String] = configuration.getString("mongodb.collections.messages.name")
}
