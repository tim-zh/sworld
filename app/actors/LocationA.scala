package actors

import akka.actor._
import models.GameEntity
import play.libs.Akka

import scala.collection.mutable

object LocationA {

	case class Enter(entity: GameEntity)
	object Leave

	case class LookupEntities(x: Double, y: Double, radius: Double, param: AnyRef)
	case class LookupEntitiesResult(entities: Map[GameEntity, ActorRef], param: AnyRef)

	case class SendMessage[+T](to: GameEntity, msg: T)

	case class CreateEntity(clazz: Class[_], entity: GameEntity)

	case class MoveEntity(x: Double, y: Double)

	case class Broadcast(msg: String, radius: Double)
	case class BroadcastChat(msg: String)

	def create(name: String, dao: ActorRef, width: Int, height: Int, cellSize: Int) = Akka.system().actorOf(Props(classOf[LocationA], dao, width, height, cellSize), name)
}

class LocationA(dao: ActorRef, width: Int, height: Int, cellSize: Int) extends Actor {
	import actors.LocationA._

	private var actorsMap = Map[ActorRef, GameEntity]()
	private var entitiesMap = Map[GameEntity, ActorRef]()
	private var entitiesGridMap = mutable.Map[GameEntity, mutable.Set[GameEntity]]()

	private val grid: Array[Array[mutable.Set[GameEntity]]] =
		for (i <- Array.range(0, width))
			yield for (j <- Array.range(0, height))
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
			filterNearbyEntities((entity.x, entity.y), radius) map { _._2 ! GameEntityA.Listen(entity, msg) }

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
			entitiesMap
 		else {
			val minX = math.max(((xy._1 - radius) / cellSize).toInt, 0)
			val maxX = math.min(((xy._1 + radius) / cellSize).toInt, width)
			val minY = math.max(((xy._2 - radius) / cellSize).toInt, 0)
			val maxY = math.min(((xy._2 + radius) / cellSize).toInt, height)
			val entitiesSeq: IndexedSeq[GameEntity] = for {
				x <- minX to maxX
				y <- minY to maxY
				entity <- grid(x)(y)
				if getDistance(xy, (entity.x, entity.y)) <= radius
			} yield entity
			(entitiesSeq map { entity => (entity, entitiesMap(entity)) }).toMap
		}

 	def getDistance(xy0: (Double, Double), xy1: (Double, Double)) =
 		Math.sqrt((xy0._1 - xy1._1) * (xy0._1 - xy1._1) + (xy0._2 - xy1._2) * (xy0._2 - xy1._2))

	def isMoveAllowed(x: Double, y: Double, entity: GameEntity) =
		0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(entity.x - x) <= 10 && Math.abs(entity.y - y) <= 10

	def updateGrid(entity: GameEntity, isLeaving: Boolean = false) {
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

	def getGridCell(x: Double, y: Double): Option[mutable.Set[GameEntity]] = {
		val i = (x / cellSize).toInt
		val j = (y / cellSize).toInt
		if (i >= 0 && j >= 0 && i < width && j < height)
			Some(grid(i)(j))
		else
			None
	}
}
