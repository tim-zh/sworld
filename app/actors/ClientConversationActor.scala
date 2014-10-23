package actors

import akka.actor._
import models.User
import play.api.libs.json.{JsBoolean, JsObject, JsValue, Json}

class ClientConversationActor(out: ActorRef, var location: ActorRef, owner: User) extends Actor {
  override def preStart() {
    location ! EnterLocation
  }

  def receive = {
    case jsObj: JsObject if jsObj.value contains "newLocation" =>
      val path = "/user/" + (jsObj \ "newLocation").as[String]
      context.actorSelection(path) ! EnterLocation
    case ConfirmEnterLocation if location != sender =>
      location ! LeaveLocation
      location = sender
      out ! Json.obj("newLocation" -> sender.path.name)
    case jsObj: JsObject if jsObj.value contains "move" =>
      val newX = (jsObj \ "move" \ "x").as[Double]
      val newY = (jsObj \ "move" \ "y").as[Double]
      location ! Move(newX, newY)
    case ConfirmMove(x, y) =>
      out ! Json.obj("move" -> Json.obj("x" -> x, "y" -> y))
    case msg: JsValue =>
      location ! ChatMessage(owner, (msg \ "text").as[String])
    case ChatMessage(user, msg) if sender == location =>
      var message = Json.obj("text" -> msg, "user" -> user.name)
      if (user.id == owner.id)
        message = message + ("isOwner" -> JsBoolean(true))
      out ! message
  }
}