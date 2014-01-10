package controllers

import datastore.BSONProperties.{MailboxIdProperty, MessageIdProperty}
import datastore.ReactiveMongoDatastore
import models.{Mailbox, Message}

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.libs.json._

import java.math.BigInteger
import java.security.MessageDigest

import org.joda.time.Instant

import reactivemongo.bson.{BSONObjectID, BSONString}

import scala.concurrent.Future
import scala.collection.immutable.Queue
import scala.collection.mutable.{HashMap, HashSet, SynchronizedMap, SynchronizedSet}

object Application extends Controller {
  val mailboxChannelSetMap = new HashMap[BSONString, SynchronizedSet[Concurrent.Channel[JsValue]]] with SynchronizedMap[BSONString, SynchronizedSet[Concurrent.Channel[JsValue]]]
  val channelMailboxSetMap = new HashMap[Concurrent.Channel[JsValue], SynchronizedSet[BSONString]] with SynchronizedMap[Concurrent.Channel[JsValue], SynchronizedSet[BSONString]]

  def uuid = java.util.UUID.randomUUID.toString()

  def toHexString(bytes: Array[Byte]): String = {
    val bigInt = new BigInteger(1, bytes)
    return String.format("%0" + (bytes.length << 1) + "X", bigInt)
  }

  val ErrorProperty = "error"
  val StatusProperty = "status"
  val ExpiryProperty = "expiry"
  val DataProperty = "data"

  private def errorMessage(str: String) = Map(ErrorProperty -> str)
  private def statusMessage(str: String) = Map(StatusProperty -> str)

  def register = Action.async { request =>
    val uuidString = uuid

    val md: MessageDigest = MessageDigest.getInstance("SHA-256");

    val idBytes = md.digest(uuidString.getBytes())

    val idHexString = toHexString(idBytes)

    val mailbox = Mailbox(Some(BSONObjectID.generate), BSONString(idHexString))

    ReactiveMongoDatastore.MailboxDAO.insert(mailbox).map(
      lastError => {
        if (lastError.ok)
          Ok(Json.toJson(Map(MailboxIdProperty -> mailbox.mailboxId.value)))
        else
          InternalServerError(Json.toJson(errorMessage("Couldn't create mailbox.")))
      }
    )
  }

  def deregister = Action.async(parse.json) { request =>
    val jsonValue = request.body

    val jsResult: JsResult[String] = (jsonValue \ MailboxIdProperty).validate[String]

    jsResult.fold(
      _ => Future(BadRequest(Json.toJson(errorMessage("Missing " + MailboxIdProperty + " property")))),
      mailboxId => {
        ReactiveMongoDatastore.MailboxDAO.removeByMailboxId(BSONString(mailboxId)).map(
          lastError => {
            if (lastError.ok && lastError.updated > 0) {
              mailboxChannelSetMap.remove(BSONString(mailboxId)).foreach(
                set => {
                  set.foreach(
                    channel => channelMailboxSetMap.get(channel).foreach(
                      set => set.remove(BSONString(mailboxId))
                    )
                  )
                }
              )
              Ok(Json.toJson(Map(MailboxIdProperty -> mailboxId)))
            }
            else
              InternalServerError(Json.toJson(errorMessage("Couldn't de-register mailbox with ID: " + mailboxId)))
          }
        )
      }
    )
  }

  def send = Action.async(parse.json) { request =>
    val jsonValue = request.body

    val jsResult: JsResult[String] = (jsonValue \ MailboxIdProperty).validate[String]

    jsResult.fold(
      _ => Future(BadRequest(Json.toJson(errorMessage("Missing " + MailboxIdProperty + " property")))),
      mailboxId => {
        ReactiveMongoDatastore.MailboxDAO.findByMailboxId(BSONString(mailboxId)).flatMap(
          mailboxOption => mailboxOption.fold(Future(BadRequest(Json.toJson(errorMessage("Non-existent mailbox with ID: " + mailboxId))))){
            mailbox => {
              val expirySeconds: Int = (jsonValue \ ExpiryProperty).validate[Int].fold(valid = (res => res), invalid = (e => 0))

              (jsonValue \ DataProperty).validate[JsValue].fold(
                valid = (res => {
                  val message = Message(Some(BSONObjectID.generate), mailbox.objectId.get, (Instant.now().getMillis()) / 1000 + expirySeconds, res)
                  val channelSet = mailboxChannelSetMap.getOrElse(mailbox.mailboxId, Set())

                  if (channelSet.isEmpty) {
                    ReactiveMongoDatastore.MessageDAO.insert(message).map(
                      lastError => {
                        if (lastError.ok)
                          Ok(Json.toJson(message))
                        else
                          InternalServerError(Json.toJson(errorMessage("Couldn't insert into mailbox with ID: " + mailboxId)))
                      }
                    )
                  } else {
                    channelSet.foreach(channel => channel.push(Json.toJson(message.data)))
                    Future(Ok(Json.toJson(message)))
                  }
                }),
                invalid = (e => Future(BadRequest(Json.toJson(errorMessage("Invalid or missing data property")))))
              )
            }
          }
        )
      }
    )
  }

  def fetchAll = Action.async(parse.json) { request =>
    val jsonValue = request.body

    val jsResult: JsResult[String] = (jsonValue \ MailboxIdProperty).validate[String]

    jsResult.fold(
      _ => Future(BadRequest(Json.toJson(errorMessage("Missing " + MailboxIdProperty + " property")))),
      mailboxId => {
        ReactiveMongoDatastore.MailboxDAO.findByMailboxId(BSONString(mailboxId)).flatMap(
          mailboxOption => mailboxOption.fold(Future(BadRequest(Json.toJson(errorMessage("Non-existent mailbox with ID: " + mailboxId))))){
            mailbox => {
              ReactiveMongoDatastore.MessageDAO.findByMailboxObjectId(mailbox.objectId.get).enumerate().run(
                Iteratee.fold[Message, JsArray](Json.arr()){ (jsonArray, message) => jsonArray :+ Json.toJson(message.data) }
              )
            }.map(jsonArray => Ok(jsonArray))
          }
        )
      }
    )
  }

  def fetchAndClearAll = Action.async(parse.json) { request =>
    val jsonValue = request.body

    val jsResult: JsResult[String] = (jsonValue \ MailboxIdProperty).validate[String]

    jsResult.fold(
      _ => Future(BadRequest(Json.toJson(errorMessage("Missing " + MailboxIdProperty + " property")))),
      mailboxId => {
        ReactiveMongoDatastore.MailboxDAO.findByMailboxId(BSONString(mailboxId)).flatMap(
          mailboxOption => mailboxOption.fold(Future(BadRequest(Json.toJson(errorMessage("Non-existent mailbox with ID: " + mailboxId))))){
            mailbox => {
              ReactiveMongoDatastore.MessageDAO.findByMailboxObjectId(mailbox.objectId.get).enumerate().run(
                Iteratee.fold[Message, Queue[Message]](Queue()){ (q, message) => q :+ message }
              )
            }.flatMap(
              q => ReactiveMongoDatastore.MessageDAO.removeMessages(q).map(_ =>
                Ok(
                  q.foldLeft(Json.arr()){ (jsonArray, message) => jsonArray :+ Json.toJson(message.data) }
                )
              )
            )
          }
        )
      }
    )
  }

  def websocket = WebSocket.using[JsValue] { request =>
    val (out, channel) = Concurrent.broadcast[JsValue]

    val in = Iteratee.foreach[JsValue] {
      msg => {
        val jsResult: JsResult[String] = (msg \ MailboxIdProperty).validate[String]

        jsResult.fold(
          _ => channel.push(Json.toJson(errorMessage("Missing " + MailboxIdProperty + " property"))),
          mailboxId => {
            ReactiveMongoDatastore.MailboxDAO.findByMailboxId(BSONString(mailboxId)).map(
              mailboxOption => mailboxOption.fold(channel.push(Json.toJson(errorMessage("Non-existent mailbox with ID: " + mailboxId)))){
                mailbox => {
                  channel.push(Json.toJson(statusMessage("Registered")))
                  mailboxChannelSetMap.getOrElseUpdate(
                    mailbox.mailboxId,
                    new HashSet[Concurrent.Channel[JsValue]]() with SynchronizedSet[Concurrent.Channel[JsValue]]
                  ).add(channel)
                  channelMailboxSetMap.getOrElseUpdate(
                    channel,
                    new HashSet[BSONString]() with SynchronizedSet[BSONString]
                  ).add(mailbox.mailboxId)
                }
              }
            )
          }
        )
      }
    }.map { _ =>
      channelMailboxSetMap.remove(channel).foreach(
        set => set.foreach(
          mailbox => mailboxChannelSetMap.get(mailbox).foreach(
            {
              set => set.remove(channel)
            }
          )
        )
      )
    }

    (in, out)
  }

}