package actors

import akka.actor.{Terminated, ActorRef, Props, Actor}
import play.libs.Akka

class Broadcaster extends Actor {
  private var subscriptors = Set[ActorRef]()

  def receive: Actor.Receive = {
    case Subscribe =>
      subscriptors += sender
      context watch sender
    case Terminated(actor) =>
      subscriptors -= actor
    case msg @ ChatMessage(_, _) =>
      subscriptors foreach(_ ! msg)
  }
}

object Broadcaster {
  val instance = Akka.system().actorOf(Props[Broadcaster])
}

