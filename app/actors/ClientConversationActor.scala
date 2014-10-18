package actors

import akka.actor._
import play.api.libs.json.JsValue
import models.User

class ClientConversationActor(out: ActorRef, broadcaster: ActorRef, owner: User) extends Actor {
  override def preStart() {
    broadcaster ! Subscribe
  }

  def receive = {
    case msg: JsValue =>
      broadcaster ! ChatMessage(owner, msg.as[String])
    case ChatMessage(user, msg) if sender == broadcaster =>
      val message = if (user.id == owner.id)
        "<b>" + user.name + "</b>: " + msg
      else
        user.name + ": " + msg
      out ! message
  }
}