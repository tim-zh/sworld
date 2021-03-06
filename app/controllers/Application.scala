package controllers

import actors._
import akka.actor._
import models.EntityType
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current
import utils.LocationInfo
import scala.concurrent.Future

object Application extends Controller {
	val daoA = DaoA.create(dao)
	val defaultLocation = LocationA.create("default", daoA, LocationInfo.default)
	val default2Location = LocationA.create("default2", daoA, LocationInfo.default2)

	def index = Action { implicit req =>
		val user = getUserFromSession
		Ok(views.html.index(user))
	}

	def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>
		val user = getUserFromSession
		Future.successful {
			if (user.isDefined)
				Right((out: ActorRef) => Props(classOf[HumanPlayerA], out, defaultLocation, user.get.gameEntity))
			else
				Left(Forbidden)
		}
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
					val userInfo = dao.addGameEntity(EntityType.player, registerData.name, "default", -1, -1, 8, 100, 100, 100)
					val newUser = dao.addUser(registerData.name, registerData.password, userInfo)
					AuthController.authUser(Some(newUser))
				} else
					Ok(views.html.register(errors, registerData.toMap))
			}
		)
	}
}