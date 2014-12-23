package actors

import akka.actor.{ActorRef, Actor}

class JsCommunicationActor(out: ActorRef, recipient: ActorRef) extends Actor {
  override def receive: Receive = {
    case _ =>
  }
}
