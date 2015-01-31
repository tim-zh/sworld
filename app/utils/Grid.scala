package utils

import models.GameEntity

import scala.collection.mutable

/**
 * Two-dimensional array of cells. Game entities are stored in cells by their position.
 * @param width horizontal number of cells
 * @param height vertical number of cells
 * @param cellSize length of cell's side
 */
class Grid(width: Int, height: Int, cellSize: Int) {
	private val entitiesGridMap = mutable.Map[GameEntity, mutable.Set[GameEntity]]()

 	private val grid: Array[Array[mutable.Set[GameEntity]]] =
 		for (i <- Array.range(0, width))
 			yield for (j <- Array.range(0, height))
 				yield mutable.Set[GameEntity]()

	private def getCell(x: Double, y: Double): Option[mutable.Set[GameEntity]] = {
 		val i = (x / cellSize).toInt
 		val j = (y / cellSize).toInt
 		if (i >= 0 && j >= 0 && i < width && j < height)
 			Some(grid(i)(j))
 		else
 			None
 	}

	private def getCells(centerX: Double, centerY: Double, radius: Double): IndexedSeq[mutable.Set[GameEntity]] = {
		val minX = math.max(((centerX - radius) / cellSize).toInt, 0)
		val maxX = math.min(((centerX + radius) / cellSize).toInt, width)
		val minY = math.max(((centerY - radius) / cellSize).toInt, 0)
		val maxY = math.min(((centerY + radius) / cellSize).toInt, height)
		val cellsSeq = for {
			x <- minX to maxX
			y <- minY to maxY
		} yield grid(x)(y)
		cellsSeq
	}

	def update(entity: GameEntity, isLeaving: Boolean = false) {
		if (isLeaving) {
			entitiesGridMap.get(entity) foreach { _ -= entity }
			entitiesGridMap -= entity
			return
		}
		val oldCell = entitiesGridMap.get(entity)
		val newCell = getCell(entity.x, entity.y)
		if (newCell.isDefined) {
			if (oldCell.isDefined) {
				if (newCell.get == oldCell.get)
					return
				oldCell.get -= entity
			}
			newCell.get += entity
			entitiesGridMap.put(entity, newCell.get)
		}
 	}

	def getEntities(x: Double, y: Double, radius: Double, filter: GameEntity => Boolean = e => true): IndexedSeq[GameEntity] =
		getCells(x, y, radius).flatten.filter(filter)
}