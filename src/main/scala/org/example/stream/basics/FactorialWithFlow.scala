package org.example.stream.basics

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.util.ByteString

import scala.concurrent.{ExecutionContextExecutor, Future}

object FactorialWithFlow extends App {
  def lineSink(filename: String): Sink[String, Future[IOResult]] =
  // Starting from a flow of strings, convert each to ByteString, then feed to the file-writing Sink
  // when chaining operations on a Source or Flow the type of the auxiliary information—called
  // the “materialized value”—is given by the leftmost starting point
  // since we want to retain what the FileIO.toPath sink has to offer, we need to say Keep.right
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  // Simple source emitting the integers 1 to 100
  val source: Source[Int, NotUsed] = Source(1 to 100)

  // Akka ActorSystem, implicit makes it available to the streams without manually passing it when running them
  implicit val system: ActorSystem = ActorSystem("QuickStart")

  // scan operator to run a computation over the whole stream
  // starting with the number 1, multiply by each of the incoming numbers
  // scan operation emits the initial value and then every calculation result
  // nothing is actually computed yet, this is a description of what needs to be computed once we run the stream
  val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)

  val result: Future[IOResult] = factorials.map(_.toString).runWith(lineSink("factorial2.txt"))

  // Terminate actor system after when stream finishes
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  result.onComplete(_ => system.terminate())
}
