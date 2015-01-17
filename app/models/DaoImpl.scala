package models

import play.api.db.DB
import play.api.Play.current
import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession

object DaoImpl extends Dao {
	val users = TableQuery[SlickUser]
	val entities = TableQuery[SlickGameEntity]
	private val db = Database.forDataSource(DB.getDataSource("embedded"))

	private val queryGetUserByNamePass = Compiled((name: Column[String], password: Column[String]) =>
		users.filter(u => u.name === name && u.password === password))

	private val queryGetUserById = Compiled((id: Column[Long]) =>
		users.filter(_.id === id))

	private val queryGetUserPasswordById = Compiled((id: Column[Long]) =>
		(for (user <- users if user.id === id) yield user).map(_.password))

	private val queryGetUserByName = Compiled((name: Column[String]) =>
		users.filter(_.name === name))

	private val queryGetEntityById = Compiled((id: Column[Long]) =>
		entities.filter(_.id === id))

	private val queryGetEntityFieldsById = Compiled((id: Column[Long]) =>
 		(for (i <- entities if i.id === id) yield i).map(x => (x.name, x.location, x.x, x.y)))


	def getUser(name: String, password: String) = db withDynTransaction { queryGetUserByNamePass(name, password).firstOption map convertUser }

	def getUser(id: Long) = db withDynTransaction { queryGetUserById(id).firstOption map convertUser }

	def getUser(name: String) = db withDynTransaction { queryGetUserByName(name).firstOption map convertUser }

	def addUser(name: String, password: String, entity: GameEntity) = db withDynTransaction {
		val id = (users.map(x => (x.name, x.password, x.entity)) returning users.map(_.id)) += (name, password, entity.id)
		User(id, 0, name, password, entity.id)
	}

	def deleteUser(id: Long) = db withDynTransaction { queryGetUserById(id).delete == 1 }

	def updateUserPassword(id: Long, password: String) = db withDynTransaction { queryGetUserPasswordById(id).update(password) == 1 }

	def getGameEntity(id: Long): Option[GameEntity] = db withDynTransaction { queryGetEntityById(id).firstOption map convertEntity }

	def addGameEntity(name: String, location: String, x: Double, y: Double) = db withDynTransaction {
		val id = (entities.map(x => (x.name, x.location, x.x, x.y)) returning entities.map(_.id)) += (name, location, x, y)
		GameEntity(id, false, name, location, x, y)
	}

 	def deleteGameEntity(id: Long) = db withDynTransaction { queryGetEntityById(id).delete == 1 }

 	def updateGameEntity(id: Long, name: String, location: String, x: Double, y: Double) = db withDynTransaction {
		queryGetEntityFieldsById(id).update((name, location, x, y)) == 1 }


	private def convertUser(d: (Long, Long, String, String, Long)) = User(d._1, d._2, d._3, d._4, d._5)

	private def convertEntity(d: (Long, String, String, Double, Double)) = GameEntity(d._1, false, d._2, d._3, d._4, d._5)
}
