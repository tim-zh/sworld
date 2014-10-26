package models

import play.api.db.DB
import play.api.Play.current
import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession

class DaoImpl extends Dao {
  val users = TableQuery[SlickUser]
  private val db = Database.forDataSource(DB.getDataSource("embedded"))

  private val queryGetUserByNamePass = Compiled((name: Column[String], password: Column[String]) =>
    users.filter(u => u.name === name && u.password === password))

  private val queryGetUserById = Compiled((id: Column[Long]) =>
    users.filter(_.id === id))

  private val queryGetUserPasswordById = Compiled((id: Column[Long]) =>
    (for (user <- users if user.id === id) yield user).map(_.password))

  private val queryGetUserPositionById = Compiled((id: Column[Long]) =>
    (for (user <- users if user.id === id) yield user).map(user => (user.location, user.x, user.y)))

  private val queryGetUserByName = Compiled((name: Column[String]) =>
    users.filter(_.name === name))


  def getUser(name: String, password: String): Option[User] =
    db withDynTransaction { queryGetUserByNamePass(name, password).firstOption map convertUser }

  def getUser(id: Long): Option[User] = db withDynTransaction { queryGetUserById(id).firstOption map convertUser}

  def getUser(name: String): Option[User] = db withDynTransaction { queryGetUserByName(name).firstOption map convertUser}

  def addUser(name: String, password: String, location: String, x: Double, y: Double): User = {
    db withDynTransaction {
      val id = (users.map(x => (x.name, x.password, x.location, x.x, x.y)) returning users.map(_.id)) += (name, password, location, x, y)
      User(id, 0, name, password, location, (x, y))
    }
  }

  def deleteUser(id: Long): Boolean = db withDynTransaction { queryGetUserById(id).delete == 1}

  def updateUserPassword(id: Long, password: String): Boolean = db withDynTransaction { queryGetUserPasswordById(id).update(password) == 1}

  def updateUserPosition(id: Long, location: String, x: Double, y: Double): Boolean = db withDynTransaction {
    queryGetUserPositionById(id).update(location, x, y) == 1
  }


  private def convertUser(d: (Long, Long, String, String, String, Double, Double)) = User(d._1, d._2, d._3, d._4, d._5, (d._6, d._7))
}
