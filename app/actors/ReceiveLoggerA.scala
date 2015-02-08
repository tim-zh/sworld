package actors

import akka.actor.{Actor, ActorLogging}

trait ReceiveLoggerA extends Actor with ActorLogging {
	protected var loggingEnabled = true

	override def aroundReceive(receive: Actor.Receive, msg: Any) {
		if (loggingEnabled)
			log.debug(msg.toString)
		super.aroundReceive(receive, msg)
	}
}
