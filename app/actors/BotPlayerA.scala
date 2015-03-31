package actors

import akka.actor.ActorRef
import models.{EntityType, GameEntity}
import utils.{LocationInfo, InfiniteUpdater, UpdateeImpl}

class BotPlayerA(initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {
	private val positionHandler = UpdateeImpl(self, entity)
	private var isRegistered = false

	override def locationEntered(newLocation: ActorRef, info: LocationInfo, entities: Map[GameEntity, ActorRef]) {
		super.locationEntered(newLocation, info, entities)
		positionHandler.setup(newLocation, isMoveAllowed)
		if (!isRegistered) {
			InfiniteUpdater.register(self, positionHandler)
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
			say("hey")
	}

	override def notifyUpdatedEntity(e: GameEntity) {
		followIfPlayer(e)
	}

	override def notifyNewEntity(e: GameEntity) {
		followIfPlayer(e)
	}

	override def notifyGoneEntity(e: GameEntity) {
		if (e.eType == EntityType.player)
			say("oh, come on!")
	}

	def followIfPlayer(e: GameEntity) {
		if (e.eType == EntityType.player && Math.hypot(entity.x - e.x, entity.y - e.y) > entity.maxSpeed)
			positionHandler.setDestination(e.x, e.y, entity.maxSpeed)
	}
}
