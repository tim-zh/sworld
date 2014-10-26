package models

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted

case class User(id: Long, version: Long, name: String, password: String, var location: String, var xy: (Double, Double))

class SlickUser(lTag: lifted.Tag) extends Table[(Long, Long, String, String, String, Double, Double)](lTag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def version = column[Long]("version", O.Default(0))
  def name = column[String]("name")
  def password = column[String]("password")
  def location = column[String]("location")
  def x = column[Double]("x", O.Default(0))
  def y = column[Double]("y", O.Default(0))
  def * = (id, version, name, password, location, x, y)
  def nameIdx = index("idx_user_name", name, unique = true)
  def namePasswordIdx = index("idx_user_name_password", (name, password), unique = true)
}