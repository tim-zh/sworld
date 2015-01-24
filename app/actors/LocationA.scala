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
	case class LookupEntitiesResult(entities: mutable.Map[GameEntity, ActorRef], param: AnyRef)

	case class SendMessage[+T](to: GameEntity, msg: T)

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
			filterNearbyEntities((entity.x, entity.y), radius) map { _._2 ! GameEntityA.Listen(entity, msg) }

		case MoveEntity(x, y) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			if (isMoveAllowed(x, y, entity)) {
				entity.x = x
				entity.y = y
				grid.update(entity)
				dao ! DaoA.UpdateEntity(entity)
				sender ! GameEntityA.MoveConfirmed(x, y)
			} else
				sender ! GameEntityA.MoveRejected(entity.x, entity.y)

		case LocationA.CreateEntity(clazz, entity) =>
			Akka.system().actorOf(Props(clazz, self, entity))
	}

	def filterNearbyEntities(xy: (Double, Double), radius: Double) =
 		if (radius == -1)
			entitiesMap
 		else {
			val entitiesSeq = grid.getEntities(xy, radius)
			val mapBuilder = mutable.Map.newBuilder[GameEntity, ActorRef]
			entitiesSeq.view.filter(e => getSquareDistance(xy, (e.x, e.y)) <= radius * radius).
					map(entity => (entity, entitiesMap(entity))).foreach(mapBuilder += _)
			mapBuilder.result()
		}

 	def getSquareDistance(xy0: (Double, Double), xy1: (Double, Double)) =
 		(xy0._1 - xy1._1) * (xy0._1 - xy1._1) + (xy0._2 - xy1._2) * (xy0._2 - xy1._2)

	def isMoveAllowed(x: Double, y: Double, entity: GameEntity) =
		0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(entity.x - x) <= 10 && Math.abs(entity.y - y) <= 10
}
