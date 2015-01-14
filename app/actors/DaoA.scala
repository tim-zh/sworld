package actors

import akka.actor.{Actor, Props}
import models.{Dao, GameEntity}
import play.libs.Akka

object DaoA {

	case class UpdateEntityPosition(entity: GameEntity)

	def create(dao: Dao) = Akka.system().actorOf(Props(classOf[DaoA], dao))
}

class DaoA(dao: Dao) extends Actor {
	override def receive = {
		case DaoA.UpdateEntityPosition(entity) =>
			dao.updateGameEntity(entity.id, entity.name, entity.location, entity.x, entity.y)
	}
}
