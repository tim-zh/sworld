package models

import play.api.db.DB
import play.api.Play.current
import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession

class Dao {
  val users = TableQuery[SlickUser]
  private val db = Database.forDataSource(DB.getDataSource("embedded"))

  def getUser(name: String, password: String): Option[User] = {
    db withDynTransaction {
      users.filter(u => u.name === name && u.password === password).firstOption map User.get
    }
  }

  def getUser(id: Long): Option[User] = {
    db withDynTransaction {
      users.filter(_.id === id).firstOption map User.get
    }
  }

  def getUser(name: String): Option[User] = {
    db withDynTransaction {
      users.filter(_.name === name).firstOption map User.get
    }
  }

  def addUser(name: String, password: String): User = {
    var id = -1L
    db withDynTransaction {
      id = (users.map(x => (x.name, x.password)) returning users.map(_.id)) += (name, password)
    }
    getUser(id).get
  }

  def deleteUser(user: Option[User]): Boolean = {
    if (!user.isDefined)
      return false
    db withDynTransaction {
      val query = for (u <- users if u.id === user.get.id) yield u
      if (query.length.run == 0)
        return false
      query.delete
      true
    }
  }

  def updateUser(id: Long, password: String): Option[User] = {
    db withDynTransaction {
      val query = for (user <- users if user.id === id) yield user
      query.map(user => user.password).update(password)
    }
    getUser(id)
  }
}
