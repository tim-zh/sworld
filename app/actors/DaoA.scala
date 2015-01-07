package actors

import akka.actor.{Actor, Props}
import models.{Dao, User}
import play.libs.Akka

object DaoA {

	case class UpdateUserPosition(user: User)

	def create(dao: Dao) = Akka.system().actorOf(Props(classOf[DaoA], dao))
}

class DaoA(dao: Dao) extends Actor {
	override def receive = {
		case DaoA.UpdateUserPosition(user) =>
			dao.updateUserPosition(user.id, user.location, user.xy._1, user.xy._2)
	}
}
