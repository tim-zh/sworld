package actors

import akka.actor.{Actor, ActorRef}

class MapA(dao: ActorRef) extends Actor {
	override def receive = {
		case LocationA.MoveUser(user, x, y) =>
			if (0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(user.xy._1 - x) <= 10 && Math.abs(user.xy._2 - y) <= 10) {
				dao ! DaoA.UpdateUserPosition(user)
				sender ! PlayerA.MoveConfirmed(x, y)
			} else
				sender ! PlayerA.MoveRejected(user.xy._1, user.xy._2)
	}
}
