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

## Using The Server

Interacting with the server is done via HTTP POST requests and the [JSON](http://en.wikipedia.org/wiki/JSON) data format. The following routes and data formats are supported:

### POST    /register
Request: The Content-Type and HTTP Body Data are currently ignored for this request.

Response: Content-Type: application/json; charset=utf-8

 The response returned upon successful registration is a JSON object containing the mailbox registration ID as illustrated below.
```json
{
  "registration_id": "296B345063A70AD82206B96760C41EA4322F12A5F66E911C56641D4CF5BB3A7B"
}
```

### POST    /deregister
Request: The request body needs to contain JSON object with the mailbox registration ID as illustrated below.
```json
{
 "registration_id": "857DE46BBBAA3BA840500986D7EE6DEF891135BC6CF643966BDFF648EC496203"
}
```
Response: Content-Type: application/json; charset=utf-8

The response returned is a Status-Code 200 (OK) if the mailbox was successfully deregistered.
```json
{
  "status": "OK"
}
```

### POST    /send
Request: The request body needs to contain a JSON object with the following properties. Note that the "data" property can be any valid JSON. This is the message data that will be delivered upon retrieval.

```json
{
  "registration_id": "C7ED71978BC4467E0F11AFEEC0078D6F6D5686FE9072110DD6DDBC28AB7656BB",
  "expiry": 10000000,
  "data": {"test": {}}
}
```
Response: Content-Type: application/json; charset=utf-8

The response returned contains the message ID as illustrated below.
```json
{
  "message_id": "57fce493-21eb-477f-b173-17d56979e265"
}
```

### POST    /fetchAll
Request: The request body needs to contain JSON object with the mailbox registration ID as illustrated below.

```json
{
  "registration_id": "857DE46BBBAA3BA840500986D7EE6DEF891135BC6CF643966BDFF648EC496203"
}
```
Response: Content-Type: application/json; charset=utf-8

The response returned contains an array of message objects as illustrated below.
```json
[
  {
    "id": "57fce493-21eb-477f-b173-17d56979e265",
    "expiry": 1397229919,
    "data": {
      "test": {}
    }
  }
]
```

### POST    /fetchAndClearAll
Request: The request body needs to contain JSON object with the mailbox registration ID as illustrated below.

```json
{
  "registration_id": "857DE46BBBAA3BA840500986D7EE6DEF891135BC6CF643966BDFF648EC496203"
}
```
Response: Content-Type: application/json; charset=utf-8

The response returned contains an array of message objects as illustrated below.
```json
[
  {
    "id": "57fce493-21eb-477f-b173-17d56979e265",
    "expiry": 1397229919,
    "data": {
      "test": {}
    }
  }
]
```

## License

This project is released under the Apache License v2, for more details see the 'LICENSE' file.

## Contributors

Marc Barry
