package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import models.Dao
import play.libs.Akka

class Location(dao: Dao) extends Actor {
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

    case Move(user, x, y) if population contains sender =>
      if (0 <= x && x <= 100 && 0 <= y && y <= 100 &&
          Math.abs(user.position.x - x) <= 1 && Math.abs(user.position.y - y) <= 1) {
        dao.updateUserPosition(user.id, user.position.location, x, y)
        sender ! ConfirmMove(x, y)
      }
      else
        sender ! (1, 1)
  }
}

object Location {
  def create(name: String, dao: Dao) = Akka.system().actorOf(Props(classOf[Location], dao), name)
}