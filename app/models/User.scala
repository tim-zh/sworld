package models

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted

case class User(name: String, password: String) extends DataEntity

object User {
  def get(t: (Long, Long, String, String)): User = {
    val x = User(t._3, t._4)
    x.id = t._1
    x.version = t._2
    x
  }
}

class SlickUser(ltag: lifted.Tag) extends Table[(Long, Long, String, String)](ltag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def version = column[Long]("version", O.Default(0))
  def name = column[String]("name")
  def password = column[String]("password")
  def * = (id, version, name, password)
  def nameIdx = index("idx_user_name", name, unique = true)
  def namePasswordIdx = index("idx_user_name_password", (name, password), unique = true)
}