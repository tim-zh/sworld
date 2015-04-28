package actors

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Props, ActorRef}
import models.{EntityType, GameEntity}
import play.libs.Akka
import utils.{LocationInfo, Utils}

object GameEntityA {

	case class LocationEntered(info: LocationInfo, entities: Set[GameEntity])

	case class Listen(from: GameEntity, msg: String)
	case class ListenChat(from: GameEntity, msg: String)

	case class NotifyEntityUpdate(entities: GameEntity)
	case class NotifyEntityDeletion(entity: GameEntity)

	case class Collision(entities: Set[GameEntity])

	private val lastTransientId = new AtomicInteger()

	def generateId() = lastTransientId.decrementAndGet()
}

abstract class GameEntityA(var location: ActorRef, entity: GameEntity) extends ReceiveLoggerA {
	import actors.GameEntityA._

	protected var currentLocationInfo: LocationInfo = _
	protected var visibleEntities = Set[GameEntity]()

	override def preStart() {
		location ! LocationA.Enter(entity.copy())
	}

	override def receive = {
		case LocationEntered(info, entities) =>
			locationEntered(sender, info, entities)
			currentLocationInfo = info
			visibleEntities = entities

		case NotifyEntityUpdate(e) =>
			if (Math.hypot(e.x - entity.x, e.y - entity.y) <= entity.viewRadius) {
				if (visibleEntities contains e)
					notifyUpdatedEntity(e)
				else {
					visibleEntities += e
					notifyNewEntity(e)
				}
			} else if (visibleEntities contains e) {
				visibleEntities -= e
				notifyGoneEntity(e)
			}

		case NotifyEntityDeletion(e) =>
			if (visibleEntities contains e) {
				visibleEntities -= e
				notifyGoneEntity(e)
			}

		case Collision(entities) =>
			entities.foreach(collideWithEntity)

		case LocationA.LookupEntitiesResult(entities) =>
			entities foreach { e =>
				if (Math.hypot(e.x - entity.x, e.y - entity.y) <= entity.viewRadius && !visibleEntities.contains(e)) {
					visibleEntities += e
					notifyNewEntity(e)
				}
			}
			visibleEntities.filter(e => !entities.contains(e)) foreach { e =>
				visibleEntities -= e
				notifyGoneEntity(e)
			}

		case ListenChat(from, msg) if sender == location =>
			listenChat(from, msg)

		case Listen(from, msg) if sender == location =>
			listen(from, msg)

		case msg =>
			handleMessage(msg)
	}

	def locationEntered(newLocation: ActorRef, info: LocationInfo, entities: Set[GameEntity]) {
		if (location != newLocation)
			location ! LocationA.Leave
		location = newLocation
		entity.location = location.path.name
	}

	def notifyUpdatedEntity(e: GameEntity) {}

	def notifyNewEntity(e: GameEntity) {}

	def notifyGoneEntity(e: GameEntity) {}

	def collideWithEntity(e: GameEntity) {}

	def listenChat(from: GameEntity, msg: String) {}

	def listen(from: GameEntity, msg: String) {}

	def handleMessage: Receive = { case _ => }

	def chat(msg: String) { location ! LocationA.BroadcastChat(msg) }

	def say(msg: String) { location ! LocationA.Broadcast(msg, entity.voiceRadius * (if (msg.endsWith("!")) 2 else 1)) }

	def setPositionAndVelocity(x: Double, y: Double, stop: Boolean) {
		val (dx, dy) = if (stop) (0d, 0d) else Utils.getVelocityVectorTo(entity, x, y)
		entity.dx = dx
		entity.dy = dy
		entity.x = x
		entity.y = y
		location ! LocationA.MoveEntity(entity)
	}

	def enterLocation(name: String) {
		if (currentLocationInfo != null && currentLocationInfo.name != name)
			context.actorSelection("/user/" + name) ! LocationA.Enter(entity.copy())
	}

	def createGameEntity(e: GameEntity) = e.eType match {
		case EntityType.bot =>
			Akka.system().actorOf(Props(classOf[BotPlayerA], location, e))
		case EntityType.grenade =>
			Akka.system().actorOf(Props(classOf[GrenadeA], location, e))
		case _ =>
			throw new IllegalStateException()
	}

	def isMoveAllowed(x: Double, y: Double, dtInMillis: Long) =
		Math.abs(entity.x - x) <= entity.maxSpeed * dtInMillis * 1.5 / 1000 &&
				Math.abs(entity.y - y) <= entity.maxSpeed * dtInMillis * 1.5 / 1000 &&
				currentLocationInfo.getCellType(x - entity.radius, y).contains(0) &&
				currentLocationInfo.getCellType(x + entity.radius, y).contains(0) &&
				currentLocationInfo.getCellType(x, y - entity.radius).contains(0) &&
				currentLocationInfo.getCellType(x, y + entity.radius).contains(0)
}