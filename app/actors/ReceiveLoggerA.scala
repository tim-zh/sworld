package actors

import akka.actor.{Actor, ActorLogging}

trait ReceiveLoggerA extends Actor with ActorLogging {
	override def aroundReceive(receive: Actor.Receive, msg: Any) {
		log.debug(msg.toString)
		super.aroundReceive(receive, msg)
	}
}
