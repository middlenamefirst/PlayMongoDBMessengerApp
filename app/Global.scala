import config.Config

import play.api.{Application, Logger, GlobalSettings}

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Application has started")

    Logger.info("<---Configuration--->")

    Config.databaseServers.fold(
      Logger.info("Database servers not provided.")
    ){
      servers => Logger.info("Database servers: " + servers)
    }

    Config.databaseName.fold(
      Logger.info("Database name not provided.")
    ){
      name => Logger.info("Database name: " + name)
    }

    Config.mailboxCollectionName.fold(
      Logger.info("Mailbox collection name not provided.")
    ){
      name => Logger.info("Mailbox collection name: " + name)
    }

    Config.messagesCollectionName.fold(
      Logger.info("Messages collection name not provided.")
    ){
      name => Logger.info("Messages collection name: " + name)
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }
}
