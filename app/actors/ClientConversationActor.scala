package actors

import akka.actor._
import play.api.libs.json.JsValue

class ClientConversationActor(out: ActorRef, broadcaster: ActorRef) extends Actor {
  override def preStart() {
    broadcaster ! Subscribe
  }

  def receive = {
    case msg: JsValue =>
      broadcaster ! ChatMessage(msg.toString)
    case ChatMessage(s) if sender == broadcaster =>
      out ! s
  }
}