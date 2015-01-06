package actors

import akka.actor.Actor
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.{TimeoutException, Await}

class MessagingA extends Actor {
	override def receive = {
		case LocationA.BroadcastChat(user, msg) =>
			val players = try {
				val result = context.parent ? LocationA.LookupPlayers(user.xy._1, user.xy._2, -1, null)
				Some(Await.result(result map { _.asInstanceOf[LocationA.LookupPlayersResult] }, 1 second))
			} catch {
				case e: TimeoutException =>
					None
			}
			players foreach {
				_.users foreach { _ ! PlayerA.ListenChat(user, msg) }
			}
	}
}
