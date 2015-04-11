package models

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted

case class GameEntity(id: Long,
											transient: Boolean,
											eType: EntityType,
											name: String,
											var location: String,
											var x: Double,
											var y: Double,
											var radius: Double,
											viewRadius: Double,
											voiceRadius: Double,
											maxSpeed: Double,
											var dx: Double = 0,
											var dy: Double = 0) {
	override def canEqual(other: Any): Boolean = other.isInstanceOf[GameEntity]

	override def equals(other: Any): Boolean = other match {
		case that: GameEntity => (that canEqual this) && id == that.id
		case _ => false
	}

	override def hashCode(): Int = id.hashCode()
 }

class SlickGameEntity(lTag: lifted.Tag) extends Table[(Long, String, String, String, Double, Double, Double, Double, Double, Double)](lTag, "game_entities") {
	def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
	def eType = column[String]("type")
	def name = column[String]("name")
	def location = column[String]("location")
	def x = column[Double]("x", O.Default(0))
	def y = column[Double]("y", O.Default(0))
	def radius = column[Double]("radius")
	def view_radius = column[Double]("view_radius", O.Default(100))
	def voice_radius = column[Double]("voice_radius", O.Default(100))
	def max_speed = column[Double]("max_speed", O.Default(100))
	def * = (id, eType, name, location, x, y, radius, view_radius, voice_radius, max_speed)
}