/*
 * Copyright 2017 Oliver Heger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.javamagazin.react.akka

import java.nio.file.{Path, Paths}
import java.util.Locale

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Example class for processing a file using Akka's streaming API.
  *
  * The file is read chunk-wise and split into single lines. Empty lines and
  * comment lines are filtered out. The lines are then converted to lowercase
  * and written to a target file.
  *
  * The main function expects the paths to the input and output files as
  * arguments.
  */
object FileProcessor {
  /** Comment start prefix. */
  private val CommentPrefix = "#"

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("Usage: FileProcessor <inputFile> <outputFile>")
      System.exit(1)
    }

    implicit val system: ActorSystem = ActorSystem("FileProcessingSystem")
    try {
      implicit val mat: ActorMaterializer = ActorMaterializer()
      val futureResult = processFile(Paths.get(args.head), Paths.get(args(1)))

      // Block to get the result of this test driver.
      // This is of course no reactive style!
      val result = Await.result(futureResult, 10.seconds)
      println(s"Written ${result.count} bytes.")
    } finally {
      Await.result(system.terminate(), 10.seconds)
      println("Actor system terminated.")
    }
  }

  /**
    * Implements file processing.
    *
    * @param input  path to the input file
    * @param output path to the output file
    * @param system the actor system
    * @param mat    the object to materialize a stream
    * @return a ''Future'' with the processing result
    */
  def processFile(input: Path, output: Path)(implicit system: ActorSystem,
                                             mat: ActorMaterializer): Future[IOResult] = {
    println(s"Processing $input to $output.")
    val source = FileIO.fromPath(input)
    val sink = FileIO.toPath(output)
    source.via(Framing.delimiter(ByteString("\r"), 1024, allowTruncation = true))
      .map(_.utf8String.trim)
      .filter(s => s.length > 0 && !s.startsWith(CommentPrefix))
      .map(s => ByteString(s.toLowerCase(Locale.ENGLISH) + System.lineSeparator()))
      .runWith(sink)
  }
}
