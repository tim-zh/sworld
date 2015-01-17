package actors

import akka.actor._
import models.GameEntity
import play.libs.Akka

import scala.collection.parallel.mutable.ParMap

object LocationA {

	case class Enter(entity: GameEntity)
	object Leave

	case class LookupEntities(x: Double, y: Double, radius: Double, param: AnyRef)
	case class LookupEntitiesResult(entities: ParMap[ActorRef, GameEntity], param: AnyRef)

	case class SendMessage[+T](to: GameEntity, msg: T)

	case class CreateEntity(clazz: Class[_], entity: GameEntity)

	case class MoveEntity(x: Double, y: Double)

	case class Broadcast(msg: String, radius: Double)
	case class BroadcastChat(msg: String)

	def create(name: String, dao: ActorRef) = Akka.system().actorOf(Props(classOf[LocationA], dao), name)
}

class LocationA(dao: ActorRef) extends Actor {
	import actors.LocationA._

	private var actorsMap = ParMap[ActorRef, GameEntity]()
	private var entitiesMap = ParMap[GameEntity, ActorRef]()

	def receive = {
		case Enter(entity) =>
			actorsMap += (sender -> entity)
			entitiesMap += (entity -> sender)
			context watch sender
			sender ! GameEntityA.LocationEntered

		case Leave =>
			entitiesMap -= actorsMap.getOrElse(sender, null)
			actorsMap -= sender

		case Terminated(actor) =>
			entitiesMap -= actorsMap.getOrElse(actor, null)
			actorsMap -= actor

		case SendMessage(to, msg) =>
			entitiesMap.get(to) foreach { _ forward msg }

		case LookupEntities(x, y, radius, param) =>
			val filtered = filterNearbyEntities((x, y), radius)
			sender ! LookupEntitiesResult(filtered, param)

		case BroadcastChat(msg) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			actorsMap foreach { _._1 ! GameEntityA.ListenChat(entity, msg) }

		case Broadcast(msg, radius) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			filterNearbyEntities((entity.x, entity.y), radius) map { _._1 ! GameEntityA.Listen(entity, msg) }

		case MoveEntity(x, y) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			if (isMoveAllowed(x, y, entity)) {
				entity.x = x
				entity.y = y
				dao ! DaoA.UpdateEntity(entity)
				sender ! GameEntityA.MoveConfirmed(x, y)
			} else
				sender ! GameEntityA.MoveRejected(entity.x, entity.y)

		case LocationA.CreateEntity(clazz, entity) =>
			Akka.system().actorOf(Props(clazz, self, entity))
	}

	def filterNearbyEntities(xy: (Double, Double), radius: Double) =
 		if (radius == -1)
			actorsMap
 		else
			actorsMap filter { entityActor => getDistance(xy, (entityActor._2.x, entityActor._2.y)) <= radius }

 	def getDistance(xy0: (Double, Double), xy1: (Double, Double)) =
 		Math.sqrt((xy0._1 - xy1._1) * (xy0._1 - xy1._1) + (xy0._2 - xy1._2) * (xy0._2 - xy1._2))

	def isMoveAllowed(x: Double, y: Double, entity: GameEntity) =
		0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(entity.x - x) <= 10 && Math.abs(entity.y - y) <= 10
}
