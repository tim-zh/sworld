package models

trait Dao {
  def getUser(name: String, password: String): Option[User]

  def getUser(id: Long): Option[User]

  def getUser(name: String): Option[User]

  def addUser(name: String, password: String, location: String, x: Double, y: Double): User

  def deleteUser(id: Long): Boolean

  def updateUserPassword(id: Long, password: String): Boolean

  def updateUserPosition(id: Long, location: String, x: Double, y: Double): Boolean
}
