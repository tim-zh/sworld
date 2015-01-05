package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import models.{User, Dao}
import play.libs.Akka

object LocationA {
	case class Enter(user: User)
	object Leave

	case class MoveUser(user: User, x: Double, y: Double)

	case class Broadcast(user: User, msg: String)
	case class BroadcastChat(user: User, msg: String)

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
		case LocationA.Enter(user) =>
			actorsMap += (sender -> user)
			usersMap += (user -> sender)
			context watch sender
			sender ! PlayerA.LocationEntered

		case LocationA.Leave =>
			usersMap -= actorsMap.getOrElse(sender, null)
			actorsMap -= sender

		case Terminated(actor) =>
			usersMap -= actorsMap.getOrElse(sender, null)
			actorsMap -= actor

		case LocationA.BroadcastChat(user, msg) if actorsMap contains sender =>
			actorsMap foreach(_._1 ! PlayerA.ListenChat(user, msg))

		case LocationA.Broadcast(user, msg) if actorsMap contains sender =>
			val user = actorsMap(sender)
			LocationA.filterNearbyUsers(user, usersMap.keySet toSeq, 4) map { usersMap(_) ! PlayerA.Listen(user, msg) }

		case LocationA.MoveUser(user, x, y) if actorsMap contains sender =>
			if (0 <= x && x <= 100 && 0 <= y && y <= 100 && Math.abs(user.xy._1 - x) <= 1 && Math.abs(user.xy._2 - y) <= 1) {
				dao.updateUserPosition(user.id, user.location, x, y)
				sender ! PlayerA.MoveConfirmed(x, y)
			} else
				sender ! PlayerA.MoveRejected(1, 1)
	}
}
