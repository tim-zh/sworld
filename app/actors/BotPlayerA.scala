package actors

import akka.actor.ActorRef
import models.GameEntity

class BotPlayerA(initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {
	override def listenChat(from: GameEntity, msg: String) {
		if (from != entity && msg == "hi")
			chat("hi")
	}

	override def listen(from: GameEntity, msg: String) {
		if (from != entity && msg == "hi")
			say("hi", 40)
	}

	override def lookAround(entities: Map[GameEntity, ActorRef], oldEntities: Map[GameEntity, ActorRef]) {
		val player = entities.find(_._1.eType == "player").map(_._1)
		if (player.isDefined && Math.hypot(entity.x - player.get.x, entity.y - player.get.y) > entity.maxSpeed) {
			val (dx, dy) = getVelocityVectorTo(player.get.x, player.get.y)
			move(entity.x + dx, entity.y + dy)
		}
	}
}
