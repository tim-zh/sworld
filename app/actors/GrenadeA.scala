package actors

import akka.actor.ActorRef
import models.GameEntity
import utils.InfiniteUpdater

class GrenadeA(initialLocation: ActorRef, entity: GameEntity) extends CollidableEntityA(initialLocation, entity) {
	override def collideWithWall() {
		say("oh")
		InfiniteUpdater.remove(self)
		context.stop(self)
	}

	override def collideWithEntity(e: GameEntity) {
		say("hey")
		InfiniteUpdater.remove(self)
		context.stop(self)
	}
}
