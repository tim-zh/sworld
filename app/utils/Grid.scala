package utils

import models.GameEntity

import scala.collection.mutable

/**
 * Two-dimensional array of cells. Game entities are stored in cells by their position.
 * @param width horizontal number of cells
 * @param height vertical number of cells
 * @param cellSize length of cell's side
 */
abstract class Grid(width: Int, height: Int, cellSize: Int) {

	protected class Cell extends mutable.HashSet[GameEntity]

	private val entitiesMap = mutable.Map[GameEntity, mutable.Set[Cell]]()
 	private val grid: Array[Array[Cell]] =
 		for (i <- Array.range(0, width))
 			yield for (j <- Array.range(0, height))
 				yield new Cell()

	protected def getCell(x: Double, y: Double): Option[Cell] = {
 		val i = (x / cellSize).toInt
 		val j = (y / cellSize).toInt
 		if (i >= 0 && j >= 0 && i < width && j < height)
 			Some(grid(i)(j))
 		else
 			None
 	}

	protected def getCells(centerX: Double, centerY: Double, radius: Double): Set[Cell] = {
		val minX = math.max(((centerX - radius) / cellSize).toInt, 0)
		val maxX = math.min(((centerX + radius) / cellSize).toInt, width - 1)
		val minY = math.max(((centerY - radius) / cellSize).toInt, 0)
		val maxY = math.min(((centerY + radius) / cellSize).toInt, height - 1)
		val cellsSeq = for {
			x <- minX to maxX
			y <- minY to maxY
		} yield grid(x)(y)
		cellsSeq.toSet
	}

	protected def getCellsFor(entity: GameEntity): Set[Cell]

	def add(entity: GameEntity) =
		getCellsFor(entity).map(cell => {
			if (entitiesMap.contains(entity))
				entitiesMap(entity) += cell
			else
				entitiesMap.put(entity, mutable.Set(cell))
			cell += entity
		}).nonEmpty

	def update(entity: GameEntity) = {
		remove(entity)
		add(entity)
	}

	def remove(entity: GameEntity) {
		entitiesMap.get(entity) foreach (_.foreach(_ -= entity))
		entitiesMap -= entity
	}

	def getEntities(x: Double, y: Double, radius: Double): Set[GameEntity] =
		getCells(x, y, radius).flatten
}

class SimpleGrid(width: Int, height: Int, cellSize: Int) extends Grid(width, height, cellSize) {
	override protected def getCellsFor(entity: GameEntity): Set[Cell] =
		getCell(entity.x, entity.y).map(Set(_)).getOrElse(Set.empty[Cell])
}

class CollisionGrid(width: Int, height: Int, cellSize: Int) extends Grid(width, height, cellSize) {
	override protected def getCellsFor(entity: GameEntity): Set[Cell] =
		getCells(entity.x, entity.y, entity.radius)

	def getCollisionsFor(entity: GameEntity) =
		getCells(entity.x, entity.y, entity.radius).flatten.filter(cellEntity =>
			entity.id != cellEntity.id &&
			Math.abs(entity.x - cellEntity.x) <= entity.radius + cellEntity.radius &&
			Math.abs(entity.y - cellEntity.y) <= entity.radius + cellEntity.radius &&
			Math.hypot(entity.x - cellEntity.x, entity.y - cellEntity.y) <= entity.radius + cellEntity.radius
		)
}
