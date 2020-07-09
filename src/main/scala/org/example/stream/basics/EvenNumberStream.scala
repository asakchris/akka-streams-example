package org.example.stream.basics

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.{ExecutionContextExecutor, Future}

object EvenNumberStream extends App {
  implicit val system: ActorSystem = ActorSystem("QuickStart")

  val numbers = 1 to 1000

  // create a Source that will iterate over the number sequence
  val numberSource: Source[Int, NotUsed] = Source.fromIterator(() => numbers.iterator)

  // Only let pass even numbers through the Flow
  val evenNumbersFlow: Flow[Int, Int, NotUsed] = Flow[Int].filter(num => num % 2 == 0)

  // Create a Source of even random numbers by combining the random number Source with the even number filter Flow
  val evenNumbersSource: Source[Int, NotUsed] = numberSource.via(evenNumbersFlow)

  // A Sink that will write its input onto the console
  val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

  // Connect the Source with the Sink and run it
  val result: Future[Done] = evenNumbersSource.runWith(consoleSink)

  // Terminate actor system after when stream finishes
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  result.onComplete(_ => system.terminate())
}
