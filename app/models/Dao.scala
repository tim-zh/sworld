package models

import play.api.db.DB
import play.api.Play.current
import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession

class Dao {
  val users = TableQuery[SlickUser]
  private val db = Database.forDataSource(DB.getDataSource("embedded"))

  private val queryGetUserByNamePass = Compiled((name: Column[String], password: Column[String]) =>
    users.filter(u => u.name === name && u.password === password))

  private val queryGetUserById = Compiled((id: Column[Long]) =>
    users.filter(_.id === id))

  private val queryGetUserFieldsById = Compiled((id: Column[Long]) =>
    (for (user <- users if user.id === id) yield user).map(_.password))

  private val queryGetUserByName = Compiled((name: Column[String]) =>
    users.filter(_.name === name))


  def getUser(name: String, password: String): Option[User] =
    db withDynTransaction { queryGetUserByNamePass(name, password).firstOption map getUser }

  def getUser(id: Long): Option[User] = db withDynTransaction { queryGetUserById(id).firstOption map getUser}

  def getUser(name: String): Option[User] = db withDynTransaction { queryGetUserByName(name).firstOption map getUser}

  def addUser(name: String, password: String): User = {
    db withDynTransaction {
      val id = (users.map(x => (x.name, x.password)) returning users.map(_.id)) += (name, password)
      User(id, 0, name, password)
    }
  }

  def deleteUser(id: Long): Boolean = db withDynTransaction { queryGetUserById(id).delete == 1}

  def updateUser(id: Long, password: String): Boolean = db withDynTransaction { queryGetUserFieldsById(id).update(password) == 1}


  private def getUser(d: (Long, Long, String, String)) = User(d._1, d._2, d._3, d._4)
}
