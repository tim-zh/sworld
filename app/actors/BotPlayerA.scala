package actors

import akka.actor.ActorRef
import models.GameEntity
import utils.PositionUpdater

class BotPlayerA(initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {
	private val positionUpdater = new PositionUpdater(self, entity)

	override def locationEntered(newLocation: ActorRef) {
		if (positionUpdater.isStarted)
			positionUpdater.stop()
		positionUpdater.start(sender, isMoveAllowed, (x: Double, y: Double) => ())
	}

	override def listenChat(from: GameEntity, msg: String) {
		if (from != entity && msg == "hi")
			chat("hey")
	}

	override def listen(from: GameEntity, msg: String) {
		if (from != entity && msg == "hi")
			say("hey", 40)
	}

	override def lookAround(entities: Map[GameEntity, ActorRef], oldEntities: Map[GameEntity, ActorRef]) {
		val player = entities.find(_._1.eType == "player").map(_._1)
		if (player.isDefined && Math.hypot(entity.x - player.get.x, entity.y - player.get.y) > entity.maxSpeed)
			positionUpdater.setDestination(player.get.x, player.get.y, entity.maxSpeed)
	}
}
