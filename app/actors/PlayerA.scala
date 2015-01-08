package actors

import akka.actor.{ActorRef, Actor}
import models.User

object PlayerA {

	object LocationEntered

	case class MoveConfirmed(x: Double, y: Double)
	case class MoveRejected(x: Double, y: Double)

	case class Listen(user: User, msg: String)
	case class ListenChat(user: User, msg: String)
}

abstract class PlayerA(var location: ActorRef, owner: User) extends Actor {

	override def preStart() {
 		location ! LocationA.Enter(owner)
 	}

	override def receive = {
		case PlayerA.LocationEntered =>
			locationEntered(sender)

		case PlayerA.MoveConfirmed(x, y) =>
			moveConfirmed(x, y)

		case PlayerA.MoveRejected(x, y) =>
			moveRejected(x, y)

		case PlayerA.ListenChat(user, msg) if sender == location =>
			listenChat(user, msg)

		case PlayerA.Listen(user, msg) if sender == location =>
			listen(user, msg)

		case msg =>
			handleMessage(msg)
	}

	def locationEntered(newLocation: ActorRef) {
		location ! LocationA.Leave
		location = newLocation
	}

	def moveConfirmed(x: Double, y: Double) { owner.xy = (x, y) }

	def moveRejected(x: Double, y: Double) { owner.xy = (x, y) }

	def listenChat(user: User, msg: String) {}

	def listen(user: User, msg: String) {}

	def handleMessage: Receive = { case _ => }

	def chat(msg: String) { location ! LocationA.BroadcastChat(owner, msg) }

	def say(msg: String, radius: Double) { location ! LocationA.Broadcast(owner, msg, radius) }
	
	def move(x: Double, y: Double) { location ! LocationA.MoveUser(owner, x, y) }
	
	def enterLocation(name: String) { context.actorSelection("/user/" + name) ! LocationA.Enter(owner) }
}