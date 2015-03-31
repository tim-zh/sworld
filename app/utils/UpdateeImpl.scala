package utils

import actors.LocationA
import akka.actor.ActorRef
import models.GameEntity

case class UpdateeImpl(bot: ActorRef, entity: GameEntity, var positionThreshold: Double = 1) extends Updatee {
	var destination: (Double, Double, Double) = null
	var location: ActorRef = _
	var moveCheck: (Double, Double, Long) => Boolean = _
	var moveRejected: (Double, Double) => Unit = _
	private var stopped = true
	private var previousPosition: (Double, Double) = _

	def setup(location: ActorRef, moveCheck: (Double, Double, Long) => Boolean,
	          moveRejected: (Double, Double) => Unit = (x: Double, y: Double) => {
		          entity.dx = 0
		          entity.dy = 0
	          }) {
		this.location = location
		this.moveCheck = moveCheck
		this.moveRejected = moveRejected
		previousPosition = (entity.x, entity.y)
	}

	def setDestination(x: Double, y: Double, radius: Double) {
		destination = (x, y, radius)
		val (dx, dy) = Utils.getVelocityVectorTo(entity, x, y)
		entity.dx = dx
		entity.dy = dy
	}

	def notifyLocation() = location.tell(LocationA.MoveEntity(entity), bot)

	override def update(dt: Long) {
		if (entity.dx != 0 || entity.dy != 0) {
			stopped = false
			if (destination != null && Math.hypot(entity.x - destination._1, entity.y - destination._2) <= destination._3)
				stopEntity()
			else {
				val newX = entity.x + entity.dx * dt / 1000
				val newY = entity.y + entity.dy * dt / 1000
				if (moveCheck(newX, newY, dt)) {
					entity.x = newX
					entity.y = newY
					if (Math.abs(previousPosition._1 - entity.x) >= positionThreshold ||
							Math.abs(previousPosition._2 - entity.y) >= positionThreshold) {
						previousPosition = (entity.x, entity.y)
						notifyLocation()
					}
				} else
					moveRejected(entity.x, entity.y)
			}
		} else if (!stopped)
			stopEntity()
	}

	private def stopEntity() {
		stopped = true
		entity.dx = 0
		entity.dy = 0
		destination = null
		notifyLocation()
	}
}