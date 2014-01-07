package models

import datastore.BSONProperties._

import play.api.libs.json._

import reactivemongo.bson._
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsNumber

object Message {
  implicit object MessageJSONFormat extends Format[Message] {
    override def reads(json: JsValue): JsResult[Message] = JsSuccess(
      Message(
        (json \ MessageIdProperty).asOpt[String].fold[Option[BSONObjectID]](None)(str => Some(BSONObjectID(str))),
        BSONObjectID((json \ MailboxBSONObjectIdProperty).asOpt[String].get),
        (json \ ExpiryProperty).asOpt[Long].getOrElse(0),
        (json \ DataProperty).asOpt[JsValue].getOrElse(Json.obj()))
      )

    override def writes(message: Message): JsValue = Json.obj(
      MessageIdProperty -> JsString(message.objectId.get.stringify),
      MailboxBSONObjectIdProperty -> JsString(message.mailboxObjectId.stringify),
      ExpiryProperty -> JsNumber(message.expiry),
      DataProperty -> message.data)
  }

  implicit object MessageBSONDocumentReader extends BSONDocumentReader[Message] {
    override def read(doc: BSONDocument): Message = {
      Message(
        doc.getAs[BSONObjectID](BSONObjectIdProperty),
        doc.getAs[BSONObjectID](MailboxBSONObjectIdProperty).get,
        doc.getAs[BSONLong](ExpiryProperty).get.value,
        Json.parse(doc.getAs[BSONString](DataProperty).get.value)
       )
    }
  }

  implicit object MessageBSONDocumentWriter extends BSONDocumentWriter[Message] {
    override def write(message: Message): BSONDocument = {
      BSONDocument(
        BSONObjectIdProperty -> message.objectId.getOrElse(BSONObjectID.generate),
        MailboxBSONObjectIdProperty -> message.mailboxObjectId,
        ExpiryProperty -> message.expiry,
        DataProperty -> Json.stringify(message.data)
      )
    }
  }
}

case class Message(objectId: Option[BSONObjectID], mailboxObjectId: BSONObjectID, expiry: Long, data: JsValue) {
}