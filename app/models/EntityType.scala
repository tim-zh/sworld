package models

object EntityType {
	val player = EntityType("player")
	val bot = EntityType("bot")

	def get(name: String): EntityType = name match {
		case "player" => player
		case "bot" => bot
	}
}

case class EntityType private(name: String)