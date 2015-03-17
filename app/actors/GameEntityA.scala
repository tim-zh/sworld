package actors

import akka.actor.{Props, ActorRef}
import models.GameEntity
import play.libs.Akka
import utils.Utils

object GameEntityA {

	case class LocationEntered(entities: Map[GameEntity, ActorRef])

	case class Listen(from: GameEntity, msg: String)
	case class ListenChat(from: GameEntity, msg: String)

	case class NotifyEntityUpdate(entities: GameEntity)
	case class NotifyEntityDeletion(entity: GameEntity)

	private var lastTransientId: Long = -1

	def generateId() = synchronized { lastTransientId -= 1; lastTransientId }
}

abstract class GameEntityA(var location: ActorRef, entity: GameEntity) extends ReceiveLoggerA {
	import actors.GameEntityA._

	private var visibleEntitiesMap = Map[GameEntity, ActorRef]()

	override def preStart() {
		location ! LocationA.Enter(entity.copy())
	}

	override def receive = {
		case LocationEntered(entities) =>
			visibleEntitiesMap = entities filter (_._1.id != entity.id)
			locationEntered(sender, visibleEntitiesMap)

		case NotifyEntityUpdate(e) =>
			if (Math.hypot(e.x - entity.x, e.y - entity.y) <= entity.viewRadius) {
				if (visibleEntitiesMap contains e)
					notifyUpdatedEntity(e)
				else {
					visibleEntitiesMap += (e -> sender)
					notifyNewEntity(e)
				}
			} else if (visibleEntitiesMap contains e) {
				visibleEntitiesMap -= e
				notifyGoneEntity(e)
			}

		case NotifyEntityDeletion(e) =>
			if (visibleEntitiesMap contains e) {
				visibleEntitiesMap -= e
				notifyGoneEntity(e)
			}

		case LocationA.LookupEntitiesResult(entities) =>
			entities.keys.filter(_.id != entity.id) foreach { e =>
				if (Math.hypot(e.x - entity.x, e.y - entity.y) <= entity.viewRadius && !visibleEntitiesMap.contains(e)) {
					visibleEntitiesMap += (e -> sender)
					notifyNewEntity(e)
				}
			}
			visibleEntitiesMap.keys.filter(e => !entities.contains(e)) foreach { e =>
				visibleEntitiesMap -= e
				notifyGoneEntity(e)
			}

		case ListenChat(from, msg) if sender == location =>
			listenChat(from, msg)

		case Listen(from, msg) if sender == location =>
			listen(from, msg)

		case msg =>
			handleMessage(msg)
	}

	def locationEntered(newLocation: ActorRef, entities: Map[GameEntity, ActorRef]) {
		if (location != newLocation)
			location ! LocationA.Leave
		location = newLocation
		entity.location = location.path.name
	}

	def notifyUpdatedEntity(e: GameEntity) {}

	def notifyNewEntity(e: GameEntity) {}

	def notifyGoneEntity(e: GameEntity) {}

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

	def enterLocation(name: String) { context.actorSelection("/user/" + name) ! LocationA.Enter(entity.copy()) }

	def createGameEntity(e: GameEntity) = e.name match {
		case "bot" =>
			Akka.system().actorOf(Props(classOf[BotPlayerA], location, e))
	}

	def isMoveAllowed(x: Double, y: Double, dtInMillis: Long) =
		Math.abs(entity.x - x) <= entity.maxSpeed * dtInMillis * 1.5 / 1000 &&
				Math.abs(entity.y - y) <= entity.maxSpeed * dtInMillis * 1.5 / 1000
}