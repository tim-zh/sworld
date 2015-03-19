package actors

import akka.actor._
import models.GameEntity
import play.libs.Akka
import utils.{LocationInfo, Grid}

object LocationA {

	case class Enter(entity: GameEntity)
	object Leave

	case class LookupEntities(x: Double, y: Double, radius: Double)
	case class LookupEntitiesResult(entities: Map[GameEntity, ActorRef])

	case class Notify[+T](to: GameEntity, msg: T)
	case class NotifyArea[+T](x: Double, y: Double, radius: Double, msg: T)

	object MoveEntity {
		def apply(entity: GameEntity): MoveEntity = this(entity.x, entity.y, entity.dx, entity.dy)
	}

	case class MoveEntity(x: Double, y: Double, dx: Double = 0, dy: Double = 0)

	case class Broadcast(msg: String, radius: Double)
	case class BroadcastChat(msg: String)

	def create(name: String, dao: ActorRef, info: LocationInfo) =
		Akka.system().actorOf(Props(classOf[LocationA], dao, info), name)
}

class LocationA(dao: ActorRef, info: LocationInfo) extends ReceiveLoggerA {
	import actors.LocationA._

	private var actorsMap = Map[ActorRef, GameEntity]()
	private var entitiesMap = Map[GameEntity, ActorRef]()
	private val grid = new Grid(info.width, info.height, info.cellSize)

	def receive = {
		case Enter(entity) =>
			actorsMap += (sender -> entity)
			entitiesMap += (entity -> sender)
			grid.update(entity)
			entity.location = self.path.name
			context watch sender
			sender ! GameEntityA.LocationEntered(info, filterNearbyEntities(entity.x, entity.y, entity.viewRadius))
			notifyEntitiesAbout(entity)

		case Leave if actorsMap contains sender =>
			val entity = actorsMap(sender)
			grid.update(entity, true)
			entitiesMap -= entity
			actorsMap -= sender
			notifyEntitiesAboutDeletionOf(entity)

		case Terminated(actor) if actorsMap contains actor =>
			val entity = actorsMap(actor)
			grid.update(entity, true)
			entitiesMap -= entity
			actorsMap -= actor
			notifyEntitiesAboutDeletionOf(entity)

		case Notify(to, msg) =>
			entitiesMap.get(to) foreach { _ forward msg }

		case NotifyArea(x, y, radius, msg) =>
			filterNearbyEntities(x, y, radius) foreach { _._2 forward msg }

		case LookupEntities(x, y, radius) =>
			val filtered = filterNearbyEntities(x, y, radius)
			sender ! LookupEntitiesResult(filtered)

		case BroadcastChat(msg) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			actorsMap foreach { _._1 ! GameEntityA.ListenChat(entity.copy(), msg) }

		case Broadcast(msg, radius) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			filterNearbyEntities(entity.x, entity.y, radius) foreach { _._2 ! GameEntityA.Listen(entity.copy(), msg) }

		case MoveEntity(x, y, dx, dy) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			entity.x = x
			entity.y = y
			entity.dx = dx
			entity.dy = dy
			grid.update(entity)
			if (!entity.transient)
				dao ! DaoA.UpdateEntity(entity.copy())
			notifyEntitiesAbout(entity)
			sender ! LookupEntitiesResult(filterNearbyEntities(entity.x, entity.y, entity.viewRadius))
	}

	def filterNearbyEntities(x: Double, y: Double, radius: Double): Map[GameEntity, ActorRef] =
 		if (radius == -1)
			entitiesMap.map(e => (e._1.copy(), e._2))
 		else {
			val entitiesSeq = grid.getEntities(x, y, radius)
			entitiesSeq.view.filter(e => Math.hypot(x - e.x, y - e.y) <= radius).
					map(e => (e.copy(), entitiesMap(e))).toMap
		}

	def notifyEntitiesAbout(entity: GameEntity) {
		filterNearbyEntities(entity.x, entity.y, maxViewRadius) foreach { entry =>
			if (entry._1.id != entity.id)
				entry._2 ! GameEntityA.NotifyEntityUpdate(entity)
		}
	}

	def notifyEntitiesAboutDeletionOf(entity: GameEntity) {
		filterNearbyEntities(entity.x, entity.y, maxViewRadius) foreach { entry =>
			if (entry._1.id != entity.id)
				entry._2 ! GameEntityA.NotifyEntityDeletion(entity)
		}
	}
}
