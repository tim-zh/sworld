package actors

import akka.actor.{Actor, Props}
import models.{Dao, GameEntity}
import play.libs.Akka

object DaoA {

	case class UpdateEntity(entity: GameEntity)

	def create(dao: Dao) = Akka.system().actorOf(Props(classOf[DaoA], dao))
}

class DaoA(dao: Dao) extends Actor {
	import DaoA._

	override def receive = {
		case UpdateEntity(entity) if !entity.transient =>
			dao.updateGameEntity(entity.id, entity.eType, entity.name, entity.location, entity.x, entity.y, entity.view_radius)
	}
}
