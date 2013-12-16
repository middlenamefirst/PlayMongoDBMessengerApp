PlayMessengerApp
================

This is small HTTP based messaging server application based on the [Play Framework](http://www.playframework.com/). Via HTTP POST methods one can:

1. Register for a mailbox
2. Deregister a mailbox
3. Send messages to a mailbox
4. Retrieve messages from a mailbox

Mailboxes are assigned unique ID's which provide a reference for mailbox operations. Messages also receive unique ID's and can contain any valid JSON data type.

## Getting Started

In order to run this application the [Play Framework](http://www.playframework.com/) is required. This app is currently written to run on [Play Framework 2.2.1](http://downloads.typesafe.com/play/2.2.1/play-2.2.1.zip) but any 2.2.x version should work provided the Play plugin in the [plugins.sbt](project/plugins.sbt) file is updated to the correct version.

Once you have downloaded the Play Framework and satisfied its [requirements](http://www.playframework.com/documentation/2.2.x/Installing) you can run the application using either

    play run

or

    play start

By default Play will listen on port 9000. Once you see the following (assuming the run command is used) the server is ready to accept requests:

    [messenger] $ run
    
    --- (Running the application from SBT, auto-reloading is enabled) ---

    [info] play - Listening for HTTP on /0:0:0:0:0:0:0:0:9000

    (Server started, use Ctrl+D to stop and go back to the console...)

## License

This project is released under the Apache License v2, for more details see the 'LICENSE' file.

## Contributors

Marc Barry
