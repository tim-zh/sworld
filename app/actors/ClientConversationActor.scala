package actors

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import models.User
import play.api.libs.json.{JsBoolean, Json, JsValue}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ClientConversationActor(out: ActorRef, var location: ActorRef, owner: User) extends Actor {
  implicit val timeout = Timeout(2 seconds)

  override def preStart() {
    location ! EnterLocation
  }

  def receive = {
    case msg: JsValue =>
      (msg \ "newLocation").asOpt[String] match {
        case Some(x) =>
          val path = "/user/" + x
          (context.actorSelection(path) ? EnterLocation) map {
            case true =>
              location ! LeaveLocation
              location = sender
              out ! Json.obj("newLocation" -> path)
            case _ =>
          }
        case None =>
          location ! ChatMessage(owner, (msg \ "text").as[String])
      }
    case ChatMessage(user, msg) if sender == location =>
      var message = Json.obj("text" -> msg, "user" -> user.name)
      if (user.id == owner.id)
        message = message + ("isOwner" -> JsBoolean(true))
      out ! message
  }
}