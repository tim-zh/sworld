package actors

import akka.actor._
import models.User
import play.api.libs.json.{JsBoolean, JsObject, Json}

class HumanPlayerA(out: ActorRef, initialLocation: ActorRef, owner: User) extends PlayerA(initialLocation, owner) {

	override def preStart() {
		super.preStart()
		out ! Json.obj("move" -> Json.obj("x" -> owner.xy._1, "y" -> owner.xy._2))
	}

	override def locationEntered(newLocation: ActorRef) {
		out ! Json.obj("newLocation" -> newLocation.path.name, "move" -> Json.obj("x" -> owner.xy._1, "y" -> owner.xy._2))
	}

	override def moveRejected(x: Double, y: Double) {
		out ! Json.obj("move" -> Json.obj("x" -> x, "y" -> y))
	}

	override def listenChat(user: User, msg: String) {
		var message = Json.obj("chat" -> msg, "user" -> user.name)
		if (user.id == owner.id)
			message = message + ("isOwner" -> JsBoolean(true))
		out ! message
	}

	override def listen(user: User, msg: String) {
		var message = Json.obj("say" -> msg, "user" -> user.name)
		if (user.id == owner.id)
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
			say(msg)
	}
}