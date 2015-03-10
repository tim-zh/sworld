package utils

import actors.LocationA
import akka.actor.ActorRef
import models.GameEntity

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PositionUpdater(owner: ActorRef, entity: GameEntity, positionThreshold: Double = 1) {
	private var enabled = true
	private var destination: (Double, Double, Double) = null

	def start(location: ActorRef, moveCheck: (Double, Double, Long) => Boolean,
	          moveRejected: (Double, Double) => Unit, interval: Int = 0): Unit = Future {
		enabled = true
		var entityStopped = true

		def stopEntity() {
			entityStopped = true
			entity.dx = 0
			entity.dy = 0
			destination = null
			location.tell(new LocationA.MoveEntity(entity), owner)
		}

		var previousPosition = (entity.x, entity.y)
		var timestamp = System.currentTimeMillis

		while (enabled) {
			val dt = System.currentTimeMillis - timestamp
			timestamp = System.currentTimeMillis
			if (entity.dx != 0 || entity.dy != 0) {
				entityStopped = false
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
							location.tell(new LocationA.MoveEntity(entity), owner)
						}
					} else
						moveRejected(entity.x, entity.y)
				}
			} else if (!entityStopped)
				stopEntity()
			if (interval > 0)
				Thread.sleep(interval)
		}
	}

	def stop() = enabled = false

	def isStarted = enabled

	def setDestination(x: Double, y: Double, radius: Double) {
		destination = (x, y, radius)
		val (dx, dy) = Utils.getVelocityVectorTo(entity, x, y)
		entity.dx = dx
		entity.dy = dy
	}
}
