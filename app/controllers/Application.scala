package controllers

import models.Message
import scala.collection.mutable
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import java.security.MessageDigest
import java.math.BigInteger
import play.api.libs.json.JsValue
import org.joda.time.Instant

object Application extends Controller {

  val mailCache = new mutable.HashMap[String, mutable.SynchronizedQueue[Message]] with mutable.SynchronizedMap[String, mutable.SynchronizedQueue[Message]]

  def uuid = java.util.UUID.randomUUID.toString()

  def toHex(bytes: Array[Byte]): String = {
    val bigInt = new BigInteger(1, bytes)
    return String.format("%0" + (bytes.length << 1) + "X", bigInt)
  }

  def register = Action { request =>
    val uuidString = uuid

    val md: MessageDigest = MessageDigest.getInstance("SHA-256");

    val idBytes = md.digest(uuidString.getBytes())

    val idHexString = toHex(idBytes)

    mailCache.put(idHexString, new mutable.SynchronizedQueue[Message]()).fold(
      Ok(Json.toJson(Map("registration_id" -> idHexString))))(
        _ => InternalServerError(Json.toJson(Map("error" -> "Cache collision"))))
  }

  def deregister = Action(parse.json) { request =>
    val jsonValue = request.body

    val jsResult: JsResult[String] = (jsonValue \ "registration_id").validate[String]

    jsResult.fold(_ => BadRequest(Json.toJson(Map("error" -> "Missing registration_id property"))),
      value => mailCache.remove(value).fold(
        BadRequest(Json.toJson(Map("error" -> "Invalid registration_id property"))))(
          queue => Ok(Json.toJson(Map("status" -> "OK")))))
  }

  private def getMailQueue(jsonValue: JsValue) = {
    (jsonValue \ "registration_id").validate[String].fold(_ => None, mailCache.get(_))
  }
  
  private def putMailQueue(jsonValue: JsValue) = {
    (jsonValue \ "registration_id").validate[String].fold(_ => None, mailCache.put(_, new mutable.SynchronizedQueue[Message]()))
  }

  def send = Action(parse.json) { request =>
    val jsonValue = request.body

    getMailQueue(jsonValue) match {
      case None => BadRequest(Json.toJson(Map("error" -> "Invalid or missing registration_id property")))
      case Some(queue) => {
        val expirySeconds: Int = (jsonValue \ "expiry").validate[Int].fold(
          valid = (res => res),
          invalid = (e => 0))

        (jsonValue \ "data").validate[JsValue].fold(
          valid = (res => {
            val messageId = uuid
            queue += Message(messageId, (Instant.now().getMillis()) / 1000 + expirySeconds, res)
            Ok(Json.toJson(Map("message_id" -> messageId)))
          }),
          invalid = (e => BadRequest(Json.toJson(Map("error" -> "Invalid or missing data property")))))
      }
    }

  }

  def fetchAll = Action(parse.json) { request =>
    val jsonValue = request.body

    getMailQueue(jsonValue).fold(
      BadRequest(Json.toJson(Map("error" -> "Invalid or missing registration_id property"))))(
        queue => Ok(queue.foldLeft(Json.arr())((jsonArray, message) => jsonArray :+ Json.toJson(message))))
  }
  
  def fetchAndClearAll = Action(parse.json) { request =>
    val jsonValue = request.body

    putMailQueue(jsonValue).fold(
      BadRequest(Json.toJson(Map("error" -> "Invalid or missing registration_id property"))))(
        queue => Ok(queue.foldLeft(Json.arr())((jsonArray, message) => jsonArray :+ Json.toJson(message))))
  }

}