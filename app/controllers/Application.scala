package controllers

import akka.actor._
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.json.JsValue
import models.User
import play.api.data.{FormError, Form}
import play.api.data.Forms._
import play.cache.Cache

object Application extends Controller {
  def index = Action { implicit req =>
    val user = getUserFromSession
    Ok(views.html.index(user))
  }

  def socket = WebSocket.tryAcceptWithActor[JsValue, String] { implicit request =>
    val user = getUserFromSession
    Future.successful(
      if (user.isDefined)
        Right((out: ActorRef) => Props(new ClientConversationActor(out)))
      else
        Left(Forbidden)
    )
  }

  def register() = Action { implicit req =>
    val registerForm = Form(mapping("name" -> nonEmptyText, "pass" -> nonEmptyText, "pass2" -> nonEmptyText)
      (RegisterData.apply)(RegisterData.unapply))
    registerForm.bindFromRequest.fold(
      badForm =>
        Ok(views.html.register(if (req.method == "POST")
          badForm.errors ++ new RegisterData(badForm.data).validate else Nil, badForm.data)),
      registerData => {
        val errors = registerData.validate
        if (errors.isEmpty) {
          val newUser = new User(registerData.name, registerData.password)
          AuthController.authUser(Some(newUser))
        } else
          Ok(views.html.register(errors, registerData.toMap))
      }
    )
  }


  def getUserFromSession(implicit req: RequestHeader) = {
    val sessionId = req.session.get("user").getOrElse("-1")
    val user = Cache.get(sessionId)
    if (user == null) None else Some(user.asInstanceOf[User])
  }

  case class RegisterData(name: String, password: String, password2: String) {
    def this(badData: Map[String, String]) =
      this(badData.get("name").getOrElse(""), badData.get("pass").getOrElse(""), badData.get("pass2").getOrElse(""))

    def validate: Seq[FormError] = {
      var errors = List[FormError]()
      if (false)
        errors = FormError("name", "user already exists") :: errors
      if (password != password2)
        errors = FormError("pass2", "password mismatch") :: errors
      errors
    }

    def toMap: Map[String, String] = {
      Map("name" -> name, "pass" -> password, "pass2" -> password2)
    }
  }
}