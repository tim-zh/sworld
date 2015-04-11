package models

object EntityType {
	val player = EntityType("player")
	val bot = EntityType("bot")
	val grenade = EntityType("grenade")

	def get(name: String): EntityType = name match {
		case "player" => player
		case "bot" => bot
		case "grenade" => grenade
	}
}

case class EntityType private(name: String)