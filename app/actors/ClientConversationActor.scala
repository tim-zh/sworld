package actors

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import models.User
import play.api.libs.json.JsValue
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ClientConversationActor(out: ActorRef, var location: ActorRef, owner: User) extends Actor {
  implicit val timeout = Timeout(2 seconds)

  override def preStart() {
    location ! EnterLocation
  }

  def receive = {
    case msg: JsValue =>
      if ((msg \ "newLocation").asOpt[String].isDefined) {
        val path = "/user/" + (msg \ "newLocation").as[String]
        (context.actorSelection(path) ? EnterLocation) map {
          case true =>
            location ! LeaveLocation
            location = sender
            out ! "<b>location</b>: " + path
          case _ =>
        }
      }
      else
        location ! ChatMessage(owner, (msg \ "text").as[String])
    case ChatMessage(user, msg) if sender == location =>
      val message = if (user.id == owner.id)
        "<b>" + user.name + "</b>: " + msg
      else
        user.name + ": " + msg
      out ! message
  }
}