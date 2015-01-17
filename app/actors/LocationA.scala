package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import models.GameEntity
import play.libs.Akka

import scala.collection.parallel.mutable.ParMap

object LocationA {

	case class Enter(entity: GameEntity)
	object Leave

	case class LookupEntities(x: Double, y: Double, radius: Double, param: AnyRef)
	case class FilterEntities(x: Double, y: Double, function: GameEntity => Boolean, param: AnyRef)
	case class LookupEntitiesResult(entities: ParMap[GameEntity, ActorRef], param: AnyRef)
	case class ProcessEntities(x: Double, y: Double, radius: Double, function: GameEntity => Unit)

	case class SendMessage[+T](to: GameEntity, msg: T)

	case class MoveEntity(entity: GameEntity, x: Double, y: Double)

	case class Broadcast(from: GameEntity, msg: String, radius: Double)
	case class BroadcastChat(from: GameEntity, msg: String)

	def create(name: String, filename: String, dao: ActorRef) = Akka.system().actorOf(Props(classOf[LocationA], filename, dao), name)

	def filterNearbyEntities(xy: (Double, Double), entities: ParMap[GameEntity, ActorRef], radius: Double) =
		if (radius == -1)
			entities
		else
			entities filter { entityActor => getDistance(xy, (entityActor._1.x, entityActor._1.y)) <= radius }

	def getDistance(xy0: (Double, Double), xy1: (Double, Double)) =
		Math.sqrt((xy0._1 - xy1._1) * (xy0._1 - xy1._1) + (xy0._2 - xy1._2) * (xy0._2 - xy1._2))
}

class LocationA(tiledMapFile: String, dao: ActorRef) extends Actor {
	private var actorsMap = ParMap[ActorRef, GameEntity]()
	private var entitiesMap = ParMap[GameEntity, ActorRef]()
	private var messaging: ActorRef = null

	override def preStart() {
		messaging = context.actorOf(Props(classOf[MessagingA]))
	}

	def receive = {
		case LocationA.Enter(entity) =>
			actorsMap += (sender -> entity)
			entitiesMap += (entity -> sender)
			context watch sender
			sender ! GameEntityA.LocationEntered

		case LocationA.Leave =>
			entitiesMap -= actorsMap.getOrElse(sender, null)
			actorsMap -= sender

		case Terminated(actor) =>
			entitiesMap -= actorsMap.getOrElse(sender, null)
			actorsMap -= actor

		case LocationA.SendMessage(to, msg) =>
			entitiesMap.get(to) foreach { _ forward msg }

		case LocationA.LookupEntities(x, y, radius, param) =>
			val filtered = LocationA.filterNearbyEntities((x, y), entitiesMap, radius)
			sender ! LocationA.LookupEntitiesResult(filtered, param)

		case LocationA.FilterEntities(x, y, function, param) =>
			val filtered = entitiesMap filter { p => function(p._1) }
			sender ! LocationA.LookupEntitiesResult(filtered, param)

		case LocationA.ProcessEntities(x, y, radius, function) =>
			LocationA.filterNearbyEntities((x, y), entitiesMap, radius) foreach { p => function(p._1) }

		case broadcast @ (_: LocationA.BroadcastChat | _: LocationA.Broadcast) if actorsMap contains sender =>
			messaging forward broadcast

		case LocationA.MoveEntity(entity, x, y) if actorsMap contains sender =>
			if (0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(entity.x - x) <= 10 && Math.abs(entity.y - y) <= 10) {
				dao ! DaoA.UpdateEntityPosition(entity)
				sender ! GameEntityA.MoveConfirmed(x, y)
			} else
				sender ! GameEntityA.MoveRejected(entity.x, entity.y)
	}
}
