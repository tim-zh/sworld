package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import play.libs.Akka

class Location extends Actor {
  private var population = Set[ActorRef]()

  def receive = {
    case EnterLocation =>
      population += sender
      context watch sender
      sender ! ConfirmEnterLocation
    case LeaveLocation =>
      population -= sender
    case Terminated(actor) =>
      population -= actor
    case msg @ ChatMessage(_, _) if population contains sender =>
      population foreach(_ ! msg)
    case Move(x, y) if population contains sender =>
      if (0 <= x && x <= 100 && 0 <= y && y <= 100)
        sender ! ConfirmMove(x, y)
      else
        sender ! (0, 0)
  }
}

object Location {
  def create(name: String) = Akka.system().actorOf(Props[Location], name)
}