package actors

import akka.actor.ActorRef
import models.GameEntity
import utils.{InfiniteUpdater, LocationInfo, UpdateeImpl}

class CollidableEntityA(initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {
	private val positionHandler = new UpdateeImpl(self, entity) {
		override def notifyLocation() =
			location ! LocationA.MoveEntity(entity, false)
	}

	override def locationEntered(newLocation: ActorRef, info: LocationInfo, entities: Set[GameEntity]) {
		super.locationEntered(newLocation, info, entities)
		positionHandler.setup(newLocation, isMoveAllowed, (x: Double, y: Double) => {
			collideWithWall()
		})
		InfiniteUpdater.register(self, positionHandler)
	}

	def collideWithWall() {}
}
