package actors

import akka.actor.ActorRef
import models.GameEntity
import utils.{InfiniteUpdater, RegisteredBot}

class BotPlayerA(initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {
	private val positionUpdater = RegisteredBot(self, entity)
	private var isRegistered = false

	override def locationEntered(newLocation: ActorRef, entities: Map[GameEntity, ActorRef]) {
		super.locationEntered(newLocation, entities)
		positionUpdater.setup(newLocation, isMoveAllowed, (x: Double, y: Double) => ())
		if (!isRegistered) {
			InfiniteUpdater.register(self, positionUpdater)
			isRegistered = true
		}
		entities.keys.foreach(e => followIfPlayer(e))
	}

	override def listenChat(from: GameEntity, msg: String) {
		if (from != entity && msg == "hi")
			chat("hey")
	}

	override def listen(from: GameEntity, msg: String) {
		if (from != entity && msg == "hi")
			say("hey", 40)
	}

	override def notifyUpdatedEntity(e: GameEntity) {
		followIfPlayer(e)
	}

	override def notifyNewEntity(e: GameEntity) {
		followIfPlayer(e)
	}

	override def notifyGoneEntity(e: GameEntity) {
		if (e.eType == "player")
			say("oh, come on!", 40)
	}

	def followIfPlayer(e: GameEntity) {
		if (e.eType == "player" && Math.hypot(entity.x - e.x, entity.y - e.y) > entity.maxSpeed)
			positionUpdater.setDestination(e.x, e.y, entity.maxSpeed)
	}
}
