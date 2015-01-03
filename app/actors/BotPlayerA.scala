package actors

import akka.actor.ActorRef
import models.User

class BotPlayerA(var location: ActorRef, owner: User) extends PlayerA {

	override def preStart() {
		location ! LocationA.EnterLocation(owner)
	}

	def receive = {
		case PlayerA.EnterLocation if location != sender =>
			location ! LocationA.LeaveLocation
			location = sender

		case PlayerA.ConfirmMove(x, y) =>
			owner.xy = (x, y)

		case PlayerA.RejectMove(x, y) =>
			owner.xy = (x, y)

		case PlayerA.Chat(user, msg) if sender == location =>

		case PlayerA.Say(user, msg) if sender == location =>
	}
}
