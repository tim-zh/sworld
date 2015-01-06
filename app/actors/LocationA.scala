package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import models.{User, Dao}
import play.libs.Akka

object LocationA {
	case class Enter(user: User)
	object Leave

	case class LookupPlayers(x: Double, y: Double, radius: Double, param: AnyRef)
	case class LookupPlayersResult(users: Iterable[ActorRef], param: AnyRef)
	case class ProcessPlayers(x: Double, y: Double, radius: Double, function: User => Unit)

	case class MoveUser(user: User, x: Double, y: Double)

	case class Broadcast(user: User, msg: String, radius: Double)
	case class BroadcastChat(user: User, msg: String)

	def create(name: String, dao: Dao) = Akka.system().actorOf(Props(classOf[LocationA], dao), name)

	def filterNearbyUsers(xy: (Double, Double), users: Iterable[User], radius: Double) =
		if (radius == -1)
			users
		else
			users filter { user => getDistance(xy, user.xy) <= radius }

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

		case LocationA.LookupPlayers(x, y, radius, param) =>
			val userRefs = LocationA.filterNearbyUsers((x, y), usersMap.keySet toSeq, radius) map { usersMap(_) }
			sender ! LocationA.LookupPlayersResult(userRefs, param)

		case LocationA.ProcessPlayers(x, y, radius, function) =>
			LocationA.filterNearbyUsers((x, y), usersMap.keySet toSeq, radius) foreach function

		case LocationA.BroadcastChat(user, msg) if actorsMap contains sender =>
			actorsMap.keySet foreach { _ ! PlayerA.ListenChat(user, msg) }

		case LocationA.Broadcast(user, msg, radius) if actorsMap contains sender =>
			val user = actorsMap(sender)
			LocationA.filterNearbyUsers(user.xy, usersMap.keySet toSeq, radius) map { usersMap(_) ! PlayerA.Listen(user, msg) }

		case LocationA.MoveUser(user, x, y) if actorsMap contains sender =>
			if (0 <= x && x <= 100 && 0 <= y && y <= 100 && Math.abs(user.xy._1 - x) <= 1 && Math.abs(user.xy._2 - y) <= 1) {
				dao.updateUserPosition(user.id, user.location, x, y)
				sender ! PlayerA.MoveConfirmed(x, y)
			} else
				sender ! PlayerA.MoveRejected(1, 1)
	}
}
