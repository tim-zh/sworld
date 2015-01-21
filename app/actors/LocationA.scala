package actors

import akka.actor._
import models.GameEntity
import play.libs.Akka

import scala.collection.mutable
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

class LocationA(dao: ActorRef, width: Int, height: Int, cellSize: Int) extends Actor {
	import actors.LocationA._

	private var actorsMap = ParMap[ActorRef, GameEntity]()
	private var entitiesMap = ParMap[GameEntity, ActorRef]()
	private var entitiesGridMap = mutable.Map[GameEntity, mutable.Set[GameEntity]]()

	private val grid: Array[Array[mutable.Set[GameEntity]]] =
		for (i <- Array(0 until width / cellSize))
			yield for (j <- Array(0 until width / cellSize))
				yield mutable.Set[GameEntity]()

	def receive = {
		case Enter(entity) =>
			actorsMap += (sender -> entity)
			entitiesMap += (entity -> sender)
			updateGrid(entity)
			context watch sender
			sender ! GameEntityA.LocationEntered

		case Leave if actorsMap contains sender =>
			val entity = actorsMap(sender)
			updateGrid(entity, true)
			entitiesMap -= entity
			actorsMap -= sender

		case Terminated(actor) if actorsMap contains actor =>
			val entity = actorsMap(actor)
			updateGrid(entity, true)
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
			filterNearbyEntities((entity.x, entity.y), radius) map { _._1 ! GameEntityA.Listen(entity, msg) }

		case MoveEntity(x, y) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			if (isMoveAllowed(x, y, entity)) {
				entity.x = x
				entity.y = y
				updateGrid(entity)
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

	private def isMoveAllowed(x: Double, y: Double, entity: GameEntity) =
		0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(entity.x - x) <= 10 && Math.abs(entity.y - y) <= 10

	private def updateGrid(entity: GameEntity, isLeaving: Boolean = false) {
		if (isLeaving) {
			entitiesGridMap.get(entity) foreach { _ -= entity }
			entitiesGridMap -= entity
			return
		}
		val oldCell = entitiesGridMap.get(entity)
		val newCell = getGridCell(entity.x, entity.y)
		if (newCell.isDefined) {
			if (oldCell.isDefined) {
				if (newCell.get == oldCell.get)
					return
				oldCell.get -= entity
			}
			newCell.get += entity
			entitiesGridMap.put(entity, newCell.get)
		}
	}

	private def getGridCell(x: Double, y: Double): Option[mutable.Set[GameEntity]] = {
		val i = (x / width).toInt
		val j = (y / height).toInt
		if (i >= 0 && j >= 0 && i < grid.length && j < grid(i).length)
			Some(grid(i)(j))
		else
			None
	}
}
