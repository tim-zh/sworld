package actors

import akka.actor.{Actor, ActorRef}

class MapA(dao: ActorRef) extends Actor {
	override def receive = {
		case LocationA.MoveUser(user, x, y) =>
			if (0 <= x && x <= 100 && 0 <= y && y <= 100 && Math.abs(user.xy._1 - x) <= 1 && Math.abs(user.xy._2 - y) <= 1) {
				dao ! DaoA.UpdateUserPosition(user)
				sender ! PlayerA.MoveConfirmed(x, y)
			} else
				sender ! PlayerA.MoveRejected(1, 1)
	}
}
