package models

trait Dao {
	def getUser(name: String, password: String): Option[User]

	def getUser(id: Long): Option[User]

	def getUser(name: String): Option[User]

	def addUser(name: String, password: String, info: GameEntity): User

	def deleteUser(id: Long): Boolean

	def updateUserPassword(id: Long, password: String): Boolean

	def getGameEntity(id: Long): Option[GameEntity]

	def addGameEntity(eType: EntityType,
										name: String,
										location: String,
										x: Double,
										y: Double,
										radius: Double,
										viewRadius: Double,
										voiceRadius: Double,
										maxSpeed: Double): GameEntity

	def deleteGameEntity(id: Long): Boolean

	def updateGameEntity(id: Long,
											 eType: EntityType,
											 name: String,
											 location: String,
											 x: Double,
											 y: Double,
											 radius: Double,
											 viewRadius: Double,
											 voiceRadius: Double,
											 maxSpeed: Double): Boolean
}
