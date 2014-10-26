package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import models.{User, Dao}
import play.libs.Akka

class Location(dao: Dao) extends Actor {
  private var actorsMap = Map[ActorRef, User]()
  private var usersMap = Map[User, ActorRef]()

  def receive = {
    case EnterLocation(user) =>
      actorsMap += (sender -> user)
      usersMap += (user -> sender)
      context watch sender
      sender ! ConfirmEnterLocation

    case LeaveLocation =>
      usersMap -= actorsMap.getOrElse(sender, null)
      actorsMap -= sender

    case Terminated(actor) =>
      usersMap -= actorsMap.getOrElse(sender, null)
      actorsMap -= actor

    case msg @ ChatMessage(_, _) if actorsMap contains sender =>
      actorsMap foreach(_._1 ! msg)

    case msg @ Say(_, _) if actorsMap contains sender =>
      val user = actorsMap(sender)
      filterNearbyUsers(user, usersMap.keySet toSeq, 4) map(usersMap(_) ! msg)

    case Move(user, x, y) if actorsMap contains sender =>
      if (0 <= x && x <= 100 && 0 <= y && y <= 100 &&
          Math.abs(user.xy._1 - x) <= 1 && Math.abs(user.xy._2 - y) <= 1) {
        dao.updateUserPosition(user.id, user.location, x, y)
        sender ! ConfirmMove(x, y)
      } else
        sender ! ConfirmMove(1, 1)
  }
}

object Location {
  def create(name: String, dao: Dao) = Akka.system().actorOf(Props(classOf[Location], dao), name)
}