package controllers

import java.util.UUID
import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.cache.Cache

object AuthController extends Controller {
  def login() = Action { implicit req =>
    val loginForm = Form(mapping("name" -> nonEmptyText, "pass" -> nonEmptyText)(LoginData.apply)(LoginData.unapply))
    loginForm.bindFromRequest.fold(
      badForm =>
        Redirect("/"),
      loginData => {
        val user = dao.getUser(loginData.name, loginData.password)
        authUser(user)
      }
    )
  }

  def logout() = Action { implicit req =>
    Cache.remove(req.session.get("user").getOrElse(""))
    Redirect("/").withNewSession
  }

  def updateSession(user: User)(implicit req: Request[_]) {
    Cache.set(req.session.get("user").getOrElse("-1"), user)
  }

  def authUser(user: Option[User])(implicit req: Request[_]): Result = {
    user match {
      case Some(x) =>
        val key = UUID.randomUUID().toString
        Cache.set(key, x)
        Redirect("/").withSession(req.session +("user", key))
      case None =>
        Redirect("/")
    }
  }

  private case class LoginData(name: String, password: String)
}
