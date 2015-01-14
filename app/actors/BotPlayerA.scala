package actors

import akka.actor.ActorRef
import models.GameEntity

class BotPlayerA(initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {
	override def listenChat(from: GameEntity, msg: String) {
		if (from != from && msg == "hi")
			chat("hi")
	}

	override def listen(from: GameEntity, msg: String) {
		if (from != from && msg == "hi")
			say("hi", 4)
	}
}
