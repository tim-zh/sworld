package actors

import akka.actor._
import models.GameEntity
import play.libs.Akka
import utils._

object LocationA {

	case class Enter(entity: GameEntity)
	object Leave

	case class LookupEntities(x: Double, y: Double, radius: Double)
	case class LookupEntitiesResult(entities: Set[GameEntity])

	case class Notify[+T](to: GameEntity, msg: T)
	case class NotifyArea[+T](x: Double, y: Double, radius: Double, msg: T)

	object MoveEntity {
		def apply(entity: GameEntity): MoveEntity = apply(entity, true)
		def apply(entity: GameEntity, notifyEntities: Boolean): MoveEntity = this(entity.x, entity.y, entity.dx, entity.dy, notifyEntities)
	}

	case class MoveEntity(x: Double, y: Double, dx: Double = 0, dy: Double = 0, notifyEntities: Boolean)

	case class Broadcast(msg: String, radius: Double)
	case class BroadcastChat(msg: String)

	def create(name: String, dao: ActorRef, info: LocationInfo) =
		Akka.system().actorOf(Props(classOf[LocationA], dao, info), name)
}

class LocationA(dao: ActorRef, info: LocationInfo) extends ReceiveLoggerA with Updatee {
	import actors.LocationA._

	private var actorsMap = Map[ActorRef, GameEntity]()
	private var entitiesMap = Map[GameEntity, ActorRef]()
	private var movedEntities = Set[GameEntity]()
	private val grid = new SimpleGrid(info.width, info.height, info.cellSize)
	private val collisionGrid = new CollisionGrid(info.width, info.height, info.cellSize)

	override def preStart() {
		InfiniteUpdater.register(self, this)
	}

	def receive = {
		case Enter(entity) =>
			actorsMap += (sender -> entity)
			entitiesMap += (entity -> sender)
			grid.add(entity)
			collisionGrid.add(entity)
			entity.location = self.path.name
			context watch sender
			sender ! GameEntityA.LocationEntered(info, filterNearbyEntities(entity.x, entity.y, entity.viewRadius))
			notifyEntitiesAbout(entity)

		case Leave if actorsMap contains sender =>
			leave(sender)

		case Terminated(actor) if actorsMap contains actor =>
			leave(actor)

		case Notify(to, msg) =>
			entitiesMap.get(to) foreach { _ forward msg }

		case NotifyArea(x, y, radius, msg) =>
			filterNearbyEntities(x, y, radius) foreach { entitiesMap(_) forward msg }

		case LookupEntities(x, y, radius) =>
			val filtered = filterNearbyEntities(x, y, radius)
			sender ! LookupEntitiesResult(filtered)

		case BroadcastChat(msg) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			actorsMap foreach { _._1 ! GameEntityA.ListenChat(entity.copy(), msg) }

		case Broadcast(msg, radius) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			filterNearbyEntities(entity.x, entity.y, radius) foreach { entitiesMap(_) ! GameEntityA.Listen(entity.copy(), msg) }

		case MoveEntity(x, y, dx, dy, notifyEntities) if actorsMap contains sender =>
			val entity = actorsMap(sender)
			entity.x = x
			entity.y = y
			entity.dx = dx
			entity.dy = dy
			grid.update(entity)
			collisionGrid.update(entity)
			movedEntities += entity
			if (!entity.transient)
				dao ! DaoA.UpdateEntity(entity.copy())
			if (notifyEntities)
				notifyEntitiesAbout(entity)
			sender ! LookupEntitiesResult(filterNearbyEntities(entity.x, entity.y, entity.viewRadius))
	}

	def leave(actor: ActorRef) {
		val entity = actorsMap(actor)
		grid.remove(entity)
		collisionGrid.remove(entity)
		entitiesMap -= entity
		actorsMap -= actor
		notifyEntitiesAboutDeletionOf(entity)
	}

	def filterNearbyEntities(x: Double, y: Double, radius: Double): Set[GameEntity] =
		getEntitiesCopy(if (radius == -1)
			entitiesMap.keys.toSet
		else {
			val entities = grid.getEntities(x, y, radius)
			entities.filter(e => Math.hypot(x - e.x, y - e.y) <= radius)
		})

	def notifyEntitiesAbout(entity: GameEntity) {
		filterNearbyEntities(entity.x, entity.y, maxViewRadius) foreach { e =>
			if (e.id != entity.id)
				entitiesMap(e) ! GameEntityA.NotifyEntityUpdate(entity)
		}
	}

	def notifyEntitiesAboutDeletionOf(entity: GameEntity) {
		filterNearbyEntities(entity.x, entity.y, maxViewRadius) foreach { e =>
			if (e.id != entity.id)
				entitiesMap(e) ! GameEntityA.NotifyEntityDeletion(entity)
		}
	}

	override def update(dt: Long) {
		movedEntities.foreach(entity => {
			val collisions = collisionGrid.getCollisionsFor(entity)
			entitiesMap(entity) ! GameEntityA.Collision(getEntitiesCopy(collisions))
			collisions.foreach(e => entitiesMap(e) ! GameEntityA.Collision(getEntitiesCopy(Set(entity))))
		})
		movedEntities = Set[GameEntity]()
	}

	def getEntitiesCopy(entities: Set[GameEntity]) = entities.map(e => e.copy())
}
