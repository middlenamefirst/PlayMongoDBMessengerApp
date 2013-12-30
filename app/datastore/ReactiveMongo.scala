package datastore

import config.Config
import models.{Mailbox, Message}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONString, BSONObjectID, BSONDocument}
import reactivemongo.core.commands.LastError

object ReactiveMongoDatastore {
  private val driver = new MongoDriver

  private val connection = driver.connection(Config.databaseServersSeq.getOrElse(Seq("localhost")))

  val db: DefaultDB = connection.db(Config.databaseName.getOrElse("defaultDB"))

  def mailboxesCollection: BSONCollection = db.collection(Config.mailboxCollectionName.getOrElse("defaultMailboxesCollection"))

  def messagesCollection: BSONCollection = db.collection(Config.messagesCollectionName.getOrElse("defaultMessagesCollection"))

  object MailboxDAO {
    def findByMailboxId(id: BSONString): Future[Option[Mailbox]] = mailboxesCollection.find(BSONDocument(Mailbox.MailboxIdProperty -> id)).one[Mailbox]

    def insert(mailbox: Mailbox): Future[LastError] = mailboxesCollection.insert(mailbox)

    def removeByMailboxId(id: BSONString): Future[LastError] = mailboxesCollection.remove(BSONDocument(Mailbox.MailboxIdProperty -> id))
  }

  object MessageDAO {
    def findByObjectId(id: BSONObjectID): Future[Option[Message]] = messagesCollection.find(BSONDocument(Message.MessageBSONObjectIdProperty -> id)).one[Message]

    def findByMailboxObjectId(id: BSONObjectID): Cursor[Message] = messagesCollection.find(BSONDocument(Message.MailboxBSONObjectIdProperty -> id)).cursor[Message]

    def insert(message: Message): Future[LastError] = messagesCollection.insert(message)

    def removeByObjectId(id: BSONObjectID): Future[LastError] = messagesCollection.remove(BSONDocument(Message.MessageBSONObjectIdProperty -> id))
  }
}