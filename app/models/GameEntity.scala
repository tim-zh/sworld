package models

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted

case class GameEntity(id: Long,
											transient: Boolean,
											eType: String,
											name: String,
											var location: String,
											var x: Double,
											var y: Double,
											view_radius: Double) {
	override def hashCode(): Int = id.hashCode() //scala 2.11.1 mutable.HashMap/HashSet bug in LocationA.updateGrid - entitiesGridMap.get(entity)
}

class SlickGameEntity(lTag: lifted.Tag) extends Table[(Long, String, String, String, Double, Double, Double)](lTag, "game_entities") {
	def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
	def eType = column[String]("type")
	def name = column[String]("name")
	def location = column[String]("location")
	def x = column[Double]("x", O.Default(0))
	def y = column[Double]("y", O.Default(0))
	def view_radius = column[Double]("view_radius", O.Default(100))
	def * = (id, eType, name, location, x, y, view_radius)
}