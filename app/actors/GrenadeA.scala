package actors

import akka.actor.ActorRef
import models.GameEntity

class GrenadeA(initialLocation: ActorRef, entity: GameEntity) extends CollidableEntityA(initialLocation, entity) {
	override def collideWithWall() {
		super.collideWithWall()
		say("oh")
	}

	override def collideWithEntity(e: GameEntity) {
		super.collideWithEntity(e)
		say("hey")
	}
}
