package models

trait Dao {
  def getUser(name: String, password: String): Option[User]

  def getUser(id: Long): Option[User]

  def getUser(name: String): Option[User]

  def addUser(name: String, password: String): User

  def deleteUser(id: Long): Boolean

  def updateUser(id: Long, password: String): Boolean
}
