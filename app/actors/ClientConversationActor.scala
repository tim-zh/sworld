package actors

import akka.actor._
import models.User
import play.api.libs.json.{JsBoolean, JsObject, JsValue, Json}

class ClientConversationActor(out: ActorRef, var location: ActorRef, owner: User) extends Actor {
  override def preStart() {
    location ! EnterLocation(owner)
    out ! Json.obj("move" -> Json.obj("x" -> owner.position.x, "y" -> owner.position.y))
  }

  def receive = {
    case jsObj: JsObject if jsObj.value contains "newLocation" =>
      val path = "/user/" + (jsObj \ "newLocation").as[String]
      context.actorSelection(path) ! EnterLocation(owner)

    case ConfirmEnterLocation if location != sender =>
      location ! LeaveLocation
      location = sender
      out ! Json.obj("newLocation" -> sender.path.name, "move" -> Json.obj("x" -> owner.position.x, "y" -> owner.position.y))

    case jsObj: JsObject if jsObj.value contains "move" =>
      val newX = (jsObj \ "move" \ "x").as[Double]
      val newY = (jsObj \ "move" \ "y").as[Double]
      location ! Move(owner, newX, newY)

    case ConfirmMove(x, y) =>
      owner.position.x = x
      owner.position.y = y
      out ! Json.obj("move" -> Json.obj("x" -> x, "y" -> y))

    case jsObj: JsObject if jsObj.value contains "chat" =>
      location ! ChatMessage(owner, (jsObj \ "chat").as[String])

    case ChatMessage(user, msg) if sender == location =>
      var message = Json.obj("chat" -> msg, "user" -> user.name)
      if (user.id == owner.id)
        message = message + ("isOwner" -> JsBoolean(true))
      out ! message

    case jsObj: JsObject if jsObj.value contains "say" =>
      location ! Say(owner, (jsObj \ "say").as[String])

    case Say(user, msg) if sender == location =>
      var message = Json.obj("say" -> msg, "user" -> user.name)
      if (user.id == owner.id)
        message = message + ("isOwner" -> JsBoolean(true))
      out ! message
  }
}