package actors

import akka.actor._
import models.GameEntity
import play.api.libs.json.{JsBoolean, JsObject, Json}

class HumanPlayerA(out: ActorRef, initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {

	override def preStart() {
		super.preStart()
		out ! Json.obj("move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
	}

	override def locationEntered(newLocation: ActorRef) {
		out ! Json.obj("newLocation" -> newLocation.path.name, "move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
	}

	override def moveRejected(x: Double, y: Double) {
		out ! Json.obj("move" -> Json.obj("x" -> x, "y" -> y))
	}

	override def listenChat(from: GameEntity, msg: String) {
		var message = Json.obj("chat" -> msg, "user" -> from.name)
		if (from.id == entity.id)
			message = message + ("isOwner" -> JsBoolean(true))
		out ! message
	}

	override def listen(from: GameEntity, msg: String) {
		var message = Json.obj("say" -> msg, "user" -> from.name)
		if (from.id == entity.id)
			message = message + ("isOwner" -> JsBoolean(true))
		out ! message
	}

	override def handleMessage: Receive = {
		case jsObj: JsObject if jsObj.value contains "newLocation" =>
			val name = (jsObj \ "newLocation").as[String]
			enterLocation(name)

		case jsObj: JsObject if jsObj.value contains "move" =>
			val newX = (jsObj \ "move" \ "x").as[Double]
			val newY = (jsObj \ "move" \ "y").as[Double]
			move(newX, newY)

		case jsObj: JsObject if jsObj.value contains "chat" =>
			val msg = (jsObj \ "chat").as[String]
			chat(msg)

		case jsObj: JsObject if jsObj.value contains "say" =>
			val msg = (jsObj \ "say").as[String]
			say(msg, 4)
			if (msg == "rise")
				createGameEntity(GameEntity(-1, true, "bot", entity.location, entity.x, entity.y))
	}
}