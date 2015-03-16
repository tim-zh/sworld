package utils

import actors.LocationA
import akka.actor.ActorRef
import models.GameEntity

case class RegisteredBot(bot: ActorRef, entity: GameEntity, var positionThreshold: Double = 1) extends Updatee {
	var destination: (Double, Double, Double) = null
	var location: ActorRef = _
	var moveCheck: (Double, Double, Long) => Boolean = _
	var moveRejected: (Double, Double) => Unit = _
	private var stopped = true
	private var previousPosition: (Double, Double) = _
	private var timestamp: Long = _

	def setup(location: ActorRef, moveCheck: (Double, Double, Long) => Boolean, moveRejected: (Double, Double) => Unit) {
		this.location = location
		this.moveCheck = moveCheck
		this.moveRejected = moveRejected
		previousPosition = (entity.x, entity.y)
		timestamp = System.currentTimeMillis
	}

	def setDestination(x: Double, y: Double, radius: Double) {
		destination = (x, y, radius)
		val (dx, dy) = Utils.getVelocityVectorTo(entity, x, y)
		entity.dx = dx
		entity.dy = dy
	}

	override def update() {
		val dt = Math.min(System.currentTimeMillis - timestamp, 40)
		timestamp = System.currentTimeMillis
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
						location.tell(LocationA.MoveEntity(entity), bot)
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
		location.tell(LocationA.MoveEntity(entity), bot)
	}
}