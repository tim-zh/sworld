package utils

import models.GameEntity

object Utils {

	def getVelocityVectorTo(entity: GameEntity, x: Double, y: Double): (Double, Double) = {
		val (dx, dy) = (x - entity.x, y - entity.y)
		if (dx == 0 && dy == 0)
			return (0, 0)
		val k = entity.maxSpeed / Math.hypot(dx, dy)
		(dx * k, dy * k)
	}
}
