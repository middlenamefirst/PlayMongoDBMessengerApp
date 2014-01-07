package models

import datastore.BSONProperties._

import reactivemongo.bson._

object Mailbox {
  implicit object MailboxBSONDocumentReader extends BSONDocumentReader[Mailbox] {
    override def read(doc: BSONDocument): Mailbox = {
      Mailbox(
        doc.getAs[BSONObjectID](BSONObjectIdProperty),
        doc.getAs[BSONString](MailboxIdProperty).get
      )
    }
  }

  implicit object MailboxBSONDocumentWriter extends BSONDocumentWriter[Mailbox] {
    override def write(mailbox: Mailbox): BSONDocument = {
      BSONDocument(
        BSONObjectIdProperty -> mailbox.objectId.getOrElse(BSONObjectID.generate),
        MailboxIdProperty -> mailbox.mailboxId
      )
    }
  }
}

case class Mailbox(objectId: Option[BSONObjectID], mailboxId: BSONString) {
}
