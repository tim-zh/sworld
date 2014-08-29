package controllers

import akka.actor._
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.json.JsValue

object Application extends Controller {
  def index = Action {
    Ok(views.html.index())
  }

  def socket = WebSocket.tryAcceptWithActor[JsValue, String] { request =>
    Future.successful(
      if (true)
        Right((out: ActorRef) => Props(new ClientConversationActor(out)))
      else
        Left(Forbidden)
    )
  }
}