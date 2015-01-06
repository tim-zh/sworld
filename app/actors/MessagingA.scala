package actors

import akka.actor.Actor

class MessagingA extends Actor {
	override def receive = {
		case LocationA.BroadcastChat(user, msg) =>
			blockingGet[LocationA.LookupPlayersResult](context.parent, LocationA.LookupPlayers(user.xy._1, user.xy._2, -1, null)) foreach {
				_.users.values foreach { _ ! PlayerA.ListenChat(user, msg) }
			}

		case LocationA.Broadcast(user, msg, radius) =>
			blockingGet[LocationA.LookupPlayersResult](context.parent, LocationA.LookupPlayers(user.xy._1, user.xy._2, radius, null)) foreach {
				_.users.values foreach { _ ! PlayerA.Listen(user, msg) }
			}
	}
}
