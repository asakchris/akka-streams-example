package org.example.stream.basics

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString

import scala.concurrent.{ExecutionContextExecutor, Future}

object Factorial extends App {
  // Akka ActorSystem, implicit makes it available to the streams without manually passing it when running them
  implicit val system: ActorSystem = ActorSystem("QuickStart")

  // Simple source emitting the integers 1 to 100
  val source: Source[Int, NotUsed] = Source(1 to 100)

  // scan operator to run a computation over the whole stream
  // starting with the number 1, multiply by each of the incoming numbers
  // scan operation emits the initial value and then every calculation result
  // nothing is actually computed yet, this is a description of what needs to be computed once we run the stream
  val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)

  // Convert the resulting series of numbers into a stream of ByteString objects describing lines in a text file
  val lines: Source[ByteString, NotUsed] = factorials.map(num => ByteString(s"$num\n"))

  val result: Future[IOResult] = lines.runWith(FileIO.toPath((Paths.get("factorials.txt"))))

  // Terminate actor system after when stream finishes
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  result.onComplete(_ => system.terminate())
}
