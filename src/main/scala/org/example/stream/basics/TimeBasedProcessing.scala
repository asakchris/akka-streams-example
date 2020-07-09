package org.example.stream.basics

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

object TimeBasedProcessing extends App {
  // Akka ActorSystem, implicit makes it available to the streams without manually passing it when running them
  implicit val system: ActorSystem = ActorSystem("QuickStart")

  // Simple source emitting the integers 1 to 100
  val source: Source[Int, NotUsed] = Source(1 to 100)

  // scan operator to run a computation over the whole stream
  // starting with the number 1, multiply by each of the incoming numbers
  // scan operation emits the initial value and then every calculation result
  // nothing is actually computed yet, this is a description of what needs to be computed once we run the stream
  val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)

  val result: Future[Done] = factorials
    .zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num")
    .throttle(1, 1.second) // throttle operator to slow down the stream to 1 element per second
    .runForeach(println)

  // Terminate actor system after when stream finishes
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  result.onComplete(_ => system.terminate())
}
