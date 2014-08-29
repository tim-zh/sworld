package controllers

import akka.actor._
import play.api.Play.current
import play.api.libs.json.JsValue

class ClientConversationActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: JsValue =>
      out ! msg.toString
  }
}
