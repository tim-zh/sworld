package actors

import akka.actor.ActorRef
import models.User

//todo type bounds for actorRef
class BotPlayerA(initialLocation: ActorRef, owner: User) extends PlayerA(initialLocation, owner) {
	override def listenChat(user: User, msg: String) {
		if (user != owner && msg == "hi")
			chat("hi")
	}

	override def listen(user: User, msg: String) {
		if (user != owner && msg == "hi")
			say("hi", 4)
	}
}
