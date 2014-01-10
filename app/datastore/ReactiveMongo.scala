package datastore

import config.Config._
import models.{Mailbox, Message}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONString, BSONObjectID, BSONDocument}
import reactivemongo.core.commands.LastError

object BSONProperties {
  val BSONObjectIdProperty = "_id"
  val MessageIdProperty = "message_id"
  val MailboxBSONObjectIdProperty = "mailbox_object_id"
  val ExpiryProperty = "expiry"
  val DataProperty = "data"
  val MailboxIdProperty = "mailbox_id"
}

object ReactiveMongoDatastore {
  import BSONProperties._

  private val driver = new MongoDriver

  private val connection = driver.connection(databaseServersOrDefault)

  private val db: DefaultDB = connection.db(databaseNameOrDefault)

  object MailboxDAO {
    private val c: BSONCollection = db.collection(mailboxCollectionNameOrDefault)

    def findByMailboxId(id: BSONString): Future[Option[Mailbox]] = c.find(BSONDocument(MailboxIdProperty -> id)).one[Mailbox]

    def insert(mailbox: Mailbox): Future[LastError] = c.insert(mailbox)

    def removeByMailboxId(id: BSONString): Future[LastError] = c.remove(BSONDocument(MailboxIdProperty -> id))
  }

  object MessageDAO {
    private val c: BSONCollection = db.collection(messagesCollectionNameOrDefault)

    def findByObjectId(id: BSONObjectID): Future[Option[Message]] = c.find(BSONDocument(BSONObjectIdProperty -> id)).one[Message]

    def findByMailboxObjectId(id: BSONObjectID): Cursor[Message] = c.find(BSONDocument(MailboxBSONObjectIdProperty -> id)).cursor[Message]

    def insert(message: Message): Future[LastError] = c.insert(message)

    def removeByObjectId(id: BSONObjectID): Future[LastError] = c.remove(BSONDocument(BSONObjectIdProperty -> id))

    def removeMessages(seq: Seq[Message]): Future[LastError] =
      c.remove(BSONDocument(BSONObjectIdProperty -> BSONDocument("$in" -> seq.map(message => message.objectId.get))))
  }
}