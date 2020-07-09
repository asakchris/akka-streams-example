package org.example.stream.basics

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContextExecutor, Future}

object Stream101 extends App {
  // Akka ActorSystem, implicit makes it available to the streams without manually passing it when running them
  implicit val system: ActorSystem = ActorSystem("QuickStart")

  // Simple source emitting the integers 1 to 100
  val source: Source[Int, NotUsed] = Source(1 to 100)

  // Complement source with consumer function to print the numbers into console
  // Pass this little stream setup to an Actor that runs it
  val done: Future[Done] = source.runForeach(i => println(i))

  // Terminate actor system after when stream finishes
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  done.onComplete(_ => system.terminate())
}
