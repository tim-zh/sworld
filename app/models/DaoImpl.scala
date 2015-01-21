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
 		(for (i <- entities if i.id === id) yield i).map(e => (e.eType, e.name, e.location, e.x, e.y, e.view_radius, e.max_speed)))


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

	def addGameEntity(eType: String, name: String, location: String, x: Double, y: Double, viewRadius: Double, maxSpeed: Double) = db withDynTransaction {
		val id = (entities.map(e => (e.eType, e.name, e.location, e.x, e.y, e.view_radius, e.max_speed)) returning entities.map(_.id)) +=
				(eType, name, location, x, y, viewRadius, maxSpeed)
		GameEntity(id, false, eType, name, location, x, y, viewRadius, maxSpeed)
	}

 	def deleteGameEntity(id: Long) = db withDynTransaction { queryGetEntityById(id).delete == 1 }

 	def updateGameEntity(id: Long, eType: String, name: String, location: String, x: Double, y: Double, viewRadius: Double, maxSpeed: Double) = db withDynTransaction {
		queryGetEntityFieldsById(id).update((eType, name, location, x, y, viewRadius, maxSpeed)) == 1 }


	private def convertUser(d: (Long, Long, String, String, Long)) = User(d._1, d._2, d._3, d._4, d._5)

	private def convertEntity(d: (Long, String, String, String, Double, Double, Double, Double)) = GameEntity(d._1, false, d._2, d._3, d._4, d._5, d._6, d._7, d._8)
}
