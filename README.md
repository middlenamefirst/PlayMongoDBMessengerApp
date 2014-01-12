messenger
=========

This is HTTP based messaging server application based on the [Play Framework](http://www.playframework.com/) and [MongoDB](http://www.mongodb.org/).

Via HTTP methods and WebSockets one can:

1. Register for a mailbox
2. Deregister a mailbox
3. Send messages to a mailbox
4. Retrieve messages from a mailbox

Mailboxes are assigned unique ID's which provide a reference for mailbox operations. Messages also receive unique ID's and can contain any valid JSON data type.

## Getting Started

In order to run this application the [Play Framework](http://www.playframework.com/) and [MongoDB](http://www.mongodb.org/) are required. This app is currently written to run on [Play Framework 2.2.1](http://downloads.typesafe.com/play/2.2.1/play-2.2.1.zip) but any 2.2.x version should work provided the Play plugin in the [plugins.sbt](project/plugins.sbt) file is updated to the correct version. The application makes use of the [ReactiveMongo](http://reactivemongo.org/) Scala driver for MongoDB. This dependency is automatically pulled in by the Play Frameworks build system. 

Once you have downloaded the Play Framework and satisfied its [requirements](http://www.playframework.com/documentation/2.2.x/Installing) and have MongoDB running on your system you can run the application using either

    play run

or

    play start

By default Play will listen on port 9000. Once you see the following (assuming the run command is used) the server is ready to accept requests:

    [messenger] $ run

    --- (Running the application from SBT, auto-reloading is enabled) ---

    [info] play - Listening for HTTP on /0:0:0:0:0:0:0:0:9000

    (Server started, use Ctrl+D to stop and go back to the console...)

The applications MongoDB configuration is done within the application.conf file found within the conf directory. Here's an example of the settings:

    mongodb {
      servers=[localhost],
      database {
        name=messenger
      }
      collections {
        mailboxes {
          name=mailboxes
        }
        messages {
          name=messages
        }
      }
    }

The database.name configuration property controls the name of the database (that exists or will be dynamically created by the app). The collections properties control the names of the collections for the mailboxes and messages.

## Using The Server

Interacting with the server is done via HTTP and the [JSON](http://en.wikipedia.org/wiki/JSON) data format.

See the [Postman](http://www.getpostman.com/) JSON file under [docs](docs/Messenger.json.postman_collection) for the supported routes and formats.

## License

This project is released under the Apache License v2, for more details see the 'LICENSE' file.

## Contributors

Marc Barry
