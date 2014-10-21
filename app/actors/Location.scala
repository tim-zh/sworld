package actors

import akka.actor.{Props, Terminated, ActorRef, Actor}
import play.libs.Akka

class Location extends Actor {
  private var population = Set[ActorRef]()

  def receive = {
    case EnterLocation =>
      population += sender
      context watch sender
      sender ! true
    case LeaveLocation =>
      population -= sender
    case Terminated(actor) =>
      population -= actor
    case msg @ ChatMessage(_, _) =>
      population foreach(_ ! msg)
  }
}

object Location {
  def create(name: String) = Akka.system().actorOf(Props[Location], name)
}