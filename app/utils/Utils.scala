package utils

import models.GameEntity

object Utils {

	def getVelocityVectorTo(entity: GameEntity, x: Double, y: Double): (Double, Double) =
		getVector(entity.x, entity.y, x, y, entity.maxSpeed)

	def getVector(x1: Double, y1: Double, x2: Double, y2: Double, length: Double) = {
		val (dx, dy) = getNormalizedVector(x1, y1, x2, y2)
		(dx * length, dy * length)
	}

	def getNormalizedVector(x1: Double, y1: Double, x2: Double, y2: Double): (Double, Double) = {
		val (dx, dy) = (x2 - x1, y2 - y1)
		if (dx == 0 && dy == 0)
			return (0, 0)
		val l = Math.hypot(dx, dy)
		(dx / l, dy / l)
	}
}
