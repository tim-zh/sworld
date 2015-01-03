package actors

import akka.actor._
import models.User
import play.api.libs.json.{JsBoolean, JsObject, JsValue, Json}

class HumanPlayerA(out: ActorRef, var location: ActorRef, owner: User) extends PlayerA {
	override def preStart() {
		location ! LocationA.EnterLocation(owner)
		out ! Json.obj("move" -> Json.obj("x" -> owner.xy._1, "y" -> owner.xy._2))
	}

	def receive = {
		//from client

		case jsObj: JsObject if jsObj.value contains "newLocation" =>
			val path = "/user/" + (jsObj \ "newLocation").as[String]
			context.actorSelection(path) ! LocationA.EnterLocation(owner)

		case jsObj: JsObject if jsObj.value contains "move" =>
			val newX = (jsObj \ "move" \ "x").as[Double]
			val newY = (jsObj \ "move" \ "y").as[Double]
			location ! LocationA.Move(owner, newX, newY)

		case jsObj: JsObject if jsObj.value contains "chat" =>
			location ! LocationA.Chat(owner, (jsObj \ "chat").as[String])

		case jsObj: JsObject if jsObj.value contains "say" =>
			location ! LocationA.Say(owner, (jsObj \ "say").as[String])

		//from location

		case PlayerA.EnterLocation if location != sender =>
			location ! LocationA.LeaveLocation
			location = sender
			out ! Json.obj("newLocation" -> sender.path.name, "move" -> Json.obj("x" -> owner.xy._1, "y" -> owner.xy._2))

		case PlayerA.ConfirmMove(x, y) =>
			owner.xy = (x, y)

		case PlayerA.RejectMove(x, y) =>
			owner.xy = (x, y)
			out ! Json.obj("move" -> Json.obj("x" -> x, "y" -> y))

		case PlayerA.Chat(user, msg) if sender == location =>
			var message = Json.obj("chat" -> msg, "user" -> user.name)
			if (user.id == owner.id)
				message = message + ("isOwner" -> JsBoolean(true))
			out ! message

		case PlayerA.Say(user, msg) if sender == location =>
			var message = Json.obj("say" -> msg, "user" -> user.name)
			if (user.id == owner.id)
				message = message + ("isOwner" -> JsBoolean(true))
			out ! message
	}
}