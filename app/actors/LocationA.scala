package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import models.{User, Dao}
import play.libs.Akka

import scala.collection.parallel.mutable.ParMap

object LocationA {
	case class Enter(user: User)
	object Leave

	case class LookupPlayers(x: Double, y: Double, radius: Double, param: AnyRef)
	case class LookupPlayersResult(users: ParMap[User, ActorRef], param: AnyRef)
	case class ProcessPlayers(x: Double, y: Double, radius: Double, function: User => Unit)

	case class SendMessage[+T](user: User, msg: T)

	case class MoveUser(user: User, x: Double, y: Double)

	case class Broadcast(user: User, msg: String, radius: Double)
	case class BroadcastChat(user: User, msg: String)

	def create(name: String, dao: Dao) = Akka.system().actorOf(Props(classOf[LocationA], dao), name)

	def filterNearbyPlayers(xy: (Double, Double), users: ParMap[User, ActorRef], radius: Double) =
		if (radius == -1)
			users
		else
			users filter { user => getDistance(xy, user._1.xy) <= radius }

	def getDistance(xy0: (Double, Double), xy1: (Double, Double)) =
		Math.sqrt((xy0._1 - xy1._1) * (xy0._1 - xy1._1) + (xy0._2 - xy1._2) * (xy0._2 - xy1._2))
}

class LocationA(dao: Dao) extends Actor {
	private var actorsMap = ParMap[ActorRef, User]()
	private var usersMap = ParMap[User, ActorRef]()
	private var messaging: ActorRef = null
	private var map: ActorRef = null

	override def preStart() {
		messaging = context.actorOf(Props(classOf[MessagingA]))
		map = context.actorOf(Props(classOf[MapA]))
	}

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

		case LocationA.SendMessage(user, msg) =>
			usersMap.get(user) foreach { _ forward msg }

		case LocationA.LookupPlayers(x, y, radius, param) =>
			val filteredPlayers = LocationA.filterNearbyPlayers((x, y), usersMap, radius)
			sender ! LocationA.LookupPlayersResult(filteredPlayers, param)

		case LocationA.ProcessPlayers(x, y, radius, function) =>
			LocationA.filterNearbyPlayers((x, y), usersMap, radius) foreach { p => function(p._1) }

		case broadcast @ (_: LocationA.BroadcastChat | _: LocationA.Broadcast) if actorsMap contains sender =>
			messaging forward broadcast

		case LocationA.MoveUser(user, x, y) if actorsMap contains sender =>
			if (0 <= x && x <= 100 && 0 <= y && y <= 100 && Math.abs(user.xy._1 - x) <= 1 && Math.abs(user.xy._2 - y) <= 1) {
				dao.updateUserPosition(user.id, user.location, x, y)
				sender ! PlayerA.MoveConfirmed(x, y)
			} else
				sender ! PlayerA.MoveRejected(1, 1)
	}
}
