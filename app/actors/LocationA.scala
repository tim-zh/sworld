package actors

import akka.actor._
import models.GameEntity
import play.libs.Akka
import utils.Grid

import scala.collection.mutable

object LocationA {

	case class Enter(entity: GameEntity)
	object Leave

	case class LookupEntities(x: Double, y: Double, radius: Double, param: AnyRef)
	case class LookupEntitiesResult(entities: mutable.Map[GameEntity, ActorRef], param: AnyRef) //todo immutable messages

	case class Notify[+T](to: GameEntity, msg: T)
	case class NotifyArea[+T](x: Double, y: Double, radius: Double, msg: T)

	case class CreateEntity(clazz: Class[_], entity: GameEntity)

	case class MoveEntity(x: Double, y: Double)

	case class Broadcast(msg: String, radius: Double)
	case class BroadcastChat(msg: String)

	def create(name: String, dao: ActorRef, width: Int, height: Int, cellSize: Int) =
		Akka.system().actorOf(Props(classOf[LocationA], dao, width, height, cellSize), name)
}

class LocationA(dao: ActorRef, width: Int, height: Int, cellSize: Int) extends Actor {
	import actors.LocationA._

	private val actorsMap = mutable.Map[ActorRef, GameEntity]()
	private val entitiesMap = mutable.Map[GameEntity, ActorRef]()
	private val grid = new Grid(width, height, cellSize)

	def receive = {
		case Enter(entity) =>
			actorsMap += (sender -> entity)
			entitiesMap += (entity -> sender)
			grid.update(entity)
			context watch sender
			sender ! GameEntityA.LocationEntered

		case Leave if actorsMap contains sender =>
			val entity = actorsMap(sender)
			grid.update(entity, true)
			entitiesMap -= entity
			actorsMap -= sender

		case Terminated(actor) if actorsMap contains actor =>
			val entity = actorsMap(actor)
			grid.update(entity, true)
			entitiesMap -= entity
			actorsMap -= actor

		case Notify(to, msg) =>
			entitiesMap.get(to) foreach { _ forward msg }

		case NotifyArea(x, y, radius, msg) =>
			filterNearbyEntities(x, y, radius) foreach { _._2 forward msg }

		case LookupEntities(x, y, radius, param) =>
			val filtered = filterNearbyEntities(x, y, radius)
			sender ! LookupEntitiesResult(filtered, param)

		case BroadcastChat(msg) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			actorsMap foreach { _._1 ! GameEntityA.ListenChat(entity, msg) }

		case Broadcast(msg, radius) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			filterNearbyEntities(entity.x, entity.y, radius) foreach { _._2 ! GameEntityA.Listen(entity, msg) }

		case MoveEntity(x, y) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			entity.dx = x - entity.x
			entity.dy = y - entity.y
			entity.x = x
			entity.y = y
			grid.update(entity)
			if (!entity.transient)
				dao ! DaoA.UpdateEntity(entity)

		case LocationA.CreateEntity(clazz, entity) =>
			Akka.system().actorOf(Props(clazz, self, entity))
	}

	def filterNearbyEntities(x: Double, y: Double, radius: Double) =
 		if (radius == -1)
			entitiesMap
 		else {
			val entitiesSeq = grid.getEntities(x, y, radius)
			val mapBuilder = mutable.Map.newBuilder[GameEntity, ActorRef]
			entitiesSeq.view.filter(e => Math.hypot(x - e.x, y - e.y) <= radius).
					map(entity => (entity, entitiesMap(entity))).
					foreach(mapBuilder += _)
			mapBuilder.result()
		}
}
