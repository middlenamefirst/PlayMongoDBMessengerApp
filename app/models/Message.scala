package models

import play.api.libs.json.JsValue
import play.api.libs.json.Format
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString
import play.api.libs.json.JsArray
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess

object Message {
  implicit object MessageFormat extends Format[Message] {
    override def reads(json: JsValue): JsResult[Message] = JsSuccess(Message(
      (json \ "id").asOpt[String].getOrElse(""),
      (json \ "expiry").asOpt[Long].getOrElse(0),
      (json \ "data").asOpt[JsValue].getOrElse(Json.obj())))

    override def writes(message: Message): JsValue = Json.obj(
      "id" -> JsString(message.id),
      "expiry" -> JsNumber(message.expirySeconds),
      "data" -> message.data)
  }
}

case class Message(val id: String, val expirySeconds: Long, val data: JsValue) {
}