package models

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted

case class User(id: Long, version: Long, name: String, password: String, entityId: Long) {
	def gameEntity = DaoImpl.getGameEntity(entityId).get
}

class SlickUser(lTag: lifted.Tag) extends Table[(Long, Long, String, String, Long)](lTag, "users") {
	def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
	def version = column[Long]("version", O.Default(0))
	def name = column[String]("name")
	def password = column[String]("password")
	def entity = column[Long]("entity_id")
	def * = (id, version, name, password, entity)
	def nameIdx = index("idx_user_name", name, unique = true)
	def namePasswordIdx = index("idx_user_name_password", (name, password), unique = true)
	def entityFk = foreignKey("fk_user_game_entity", entity, DaoImpl.users)(targetColumns = _.id, onDelete = ForeignKeyAction.Cascade)
}