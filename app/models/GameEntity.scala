package models

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted

case class GameEntity(id: Long, name: String, var location: String, var x: Double, var y: Double)

class SlickGameEntity(lTag: lifted.Tag) extends Table[(Long, String, String, Double, Double)](lTag, "game_entity") {
	def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
	def name = column[String]("name")
	def location = column[String]("location")
	def x = column[Double]("x", O.Default(0))
	def y = column[Double]("y", O.Default(0))
	def * = (id, name, location, x, y)
}