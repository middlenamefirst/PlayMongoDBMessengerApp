package models

import reactivemongo.bson._

object Mailbox {
  val MailboxBSONObjectIdProperty = "_id"
  val MailboxIdProperty = "mailbox_id"

  implicit object MailboxBSONDocumentReader extends BSONDocumentReader[Mailbox] {
    override def read(doc: BSONDocument): Mailbox = {
      Mailbox(
        doc.getAs[BSONObjectID](MailboxBSONObjectIdProperty),
        doc.getAs[BSONString](MailboxIdProperty).get
      )
    }
  }

  implicit object MailboxBSONDocumentWriter extends BSONDocumentWriter[Mailbox] {
    override def write(mailbox: Mailbox): BSONDocument = {
      BSONDocument(
        MailboxBSONObjectIdProperty -> mailbox.objectId.getOrElse(BSONObjectID.generate),
        MailboxIdProperty -> mailbox.mailboxId
      )
    }
  }
}

case class Mailbox(objectId: Option[BSONObjectID], mailboxId: BSONString) {
}
