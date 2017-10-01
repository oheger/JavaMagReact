# JavaMagAkka
This repository contains example code for an article in the German
[Java Magazin](https://jaxenter.de/magazine/java-magazin) about different
reactive programming models.

The article explores the impact of reactive programming on a simple use case:
reading a file from the file system. It shows different implementations for
this task (blocking and non-blocking) using plain Java and the reactive
frameworks [Vert.x](http://vertx.io/) and [Akka](https://akka.io/).

A multi-module Gradle build is provided to build the whole project. The
examples are logically grouped into sub projects.

## Plain Java
The _plainJava_ module contains example code that only uses Java and has no
external dependencies. Main classes are provided which can be invoked
directly. The classes expect a path to a file as command line argument. They
read this file and dump the content to the console. The following examples
are available:
* `BlockingFileReader`: A simple blocking implementation to read a file.
* `AsyncFileReaderMain`: Reads a file asynchronously using the Java _nio_ 
   package.

## Vert.x
The _vertx_ module contains examples based on the Vert.x framework. Running
these classes is a bit special because they are _verticles_ that have to run
under the control of Vert.x. Therefore, the project provides the
`VertxStartup` class which starts the framework and deploys all demo
verticles. In addition, it adds the
[Vert.x Shell](http://vertx.io/docs/vertx-shell/java/), so that interaction
from the outside is possible. The verticles register themselves as listeners on
the event bus and react on string messages pointing to the files to be read or
processed. Via the shell corresponding messages can be sent, for instance:

``%bus-send react.file.read /path/to/my/file.txt``

The table below lists the demo verticles and the event bus addresses they
listen on:

| Verticle class | Event bus address | Purpose |
| -------------- | ----------------- | ------- |
| ReadFileVerticle | react.file.read | Demonstrates a simple asynchronous file read operation. |
| ProcessFileVerticleCB | react.file.process | Processes a file in multiple steps using nested callbacks. Shows the downsides of the callback approach. |
| ProcessFileVerticleFuture | react.file.process.future | Does the same processing as `ProcessFileVerticleCB`, but uses futures to combine the single steps. |

## Akka
The single example in this module, is written in Scala and demonstrates file
processing using the streaming API offered by Akka. It is an application which
can be called directly. It expects two command line arguments for the input file and the
output file.
