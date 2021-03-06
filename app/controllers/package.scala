import models.{DaoImpl, Dao, User}
import play.api.data.FormError
import play.api.mvc.RequestHeader
import play.cache.Cache

package object controllers {
	val dao: Dao = DaoImpl

	def getUserFromSession(implicit req: RequestHeader): Option[User] = {
		val sessionId = req.session.get("user").getOrElse("-1")
		val user = Cache.get(sessionId)
		if (user == null) None else Some(user.asInstanceOf[User])
	}

	case class RegisterData(name: String, password: String, password2: String) {
		def this(badData: Map[String, String]) =
			this(badData.getOrElse("name", ""), badData.getOrElse("pass", ""), badData.getOrElse("pass2", ""))

		def validate: Seq[FormError] = {
			var errors = List[FormError]()
			if (dao.getUser(name).isDefined)
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
