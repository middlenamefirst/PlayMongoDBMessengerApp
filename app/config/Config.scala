package config

import scala.collection.JavaConverters._

import play.api.Play
import play.api.Play.current

object Config {
  private val configuration = Play.configuration

  lazy val databaseServers: Option[java.util.List[java.lang.String]] = configuration.getStringList("mongodb.servers")

  lazy val databaseServersSeq: Option[Seq[String]] = databaseServers.fold[Option[Seq[String]]](None){list => Some(list.asScala)}
  lazy val databaseServersOrDefault: Seq[String] = databaseServersSeq.getOrElse(Seq("localhost"))

  lazy val databaseName: Option[java.lang.String] = configuration.getString("mongodb.database.name")
  lazy val databaseNameOrDefault: java.lang.String = databaseName.getOrElse("messenger")

  lazy val mailboxCollectionName: Option[java.lang.String] = configuration.getString("mongodb.collections.mailboxes.name")
  lazy val mailboxCollectionNameOrDefault: java.lang.String = mailboxCollectionName.getOrElse("mailboxes")

  lazy val messagesCollectionName: Option[java.lang.String] = configuration.getString("mongodb.collections.messages.name")
  lazy val messagesCollectionNameOrDefault: java.lang.String = messagesCollectionName.getOrElse("messages")
}
