package actors

import akka.actor.{Actor, Props}
import models.{Dao, GameEntity}
import play.libs.Akka

object DaoA {

	case class UpdateEntity(entity: GameEntity)

	def create(dao: Dao) = Akka.system().actorOf(Props(classOf[DaoA], dao))
}

class DaoA(dao: Dao) extends Actor {
	override def receive = {
		case DaoA.UpdateEntity(entity) if !entity.transient =>
			dao.updateGameEntity(entity.id, entity.name, entity.location, entity.x, entity.y)
	}
}
