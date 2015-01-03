package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import models.{User, Dao}
import play.libs.Akka

object LocationA {
	case class EnterLocation(user: User)
	object LeaveLocation

	case class Move(user: User, x: Double, y: Double)

	case class Say(user: User, msg: String)
	case class Chat(user: User, msg: String)

	def create(name: String, dao: Dao) = Akka.system().actorOf(Props(classOf[LocationA], dao), name)

	def filterNearbyUsers(xy: (Double, Double), users: Iterable[User], radius: Double) =
		users filter { user => getDistance(xy, user.xy) <= radius }

	def filterNearbyUsers(user: User, users: Iterable[User], radius: Double): Iterable[User] =
		filterNearbyUsers(user.xy, users, radius)

	def getDistance(xy0: (Double, Double), xy1: (Double, Double)) =
		Math.sqrt((xy0._1 - xy1._1) * (xy0._1 - xy1._1) + (xy0._2 - xy1._2) * (xy0._2 - xy1._2))
}

class LocationA(dao: Dao) extends Actor {
	private var actorsMap = Map[ActorRef, User]()
	private var usersMap = Map[User, ActorRef]()

	def receive = {
		case LocationA.EnterLocation(user) =>
			actorsMap += (sender -> user)
			usersMap += (user -> sender)
			context watch sender
			sender ! PlayerA.EnterLocation

		case LocationA.LeaveLocation =>
			usersMap -= actorsMap.getOrElse(sender, null)
			actorsMap -= sender

		case Terminated(actor) =>
			usersMap -= actorsMap.getOrElse(sender, null)
			actorsMap -= actor

		case LocationA.Chat(user, msg) if actorsMap contains sender =>
			actorsMap foreach(_._1 ! PlayerA.Chat(user, msg))

		case LocationA.Say(user, msg) if actorsMap contains sender =>
			val user = actorsMap(sender)
			LocationA.filterNearbyUsers(user, usersMap.keySet toSeq, 4) map { usersMap(_) ! PlayerA.Say(user, msg) }

		case LocationA.Move(user, x, y) if actorsMap contains sender =>
			if (0 <= x && x <= 100 && 0 <= y && y <= 100 && Math.abs(user.xy._1 - x) <= 1 && Math.abs(user.xy._2 - y) <= 1) {
				dao.updateUserPosition(user.id, user.location, x, y)
				sender ! PlayerA.ConfirmMove(x, y)
			} else
				sender ! PlayerA.RejectMove(1, 1)
	}
}
