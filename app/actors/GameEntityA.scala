package actors

import akka.actor.{Actor, ActorRef}
import models.GameEntity

object GameEntityA {

	object LocationEntered

	case class MoveConfirmed(x: Double, y: Double)
	case class MoveRejected(x: Double, y: Double)

	case class Listen(from: GameEntity, msg: String)
	case class ListenChat(from: GameEntity, msg: String)
}

abstract class GameEntityA(var location: ActorRef, entity: GameEntity) extends Actor {

	override def preStart() {
 		location ! LocationA.Enter(entity)
 	}

	override def receive = {
		case GameEntityA.LocationEntered =>
			locationEntered(sender)

		case GameEntityA.MoveConfirmed(x, y) if sender == location =>
			moveConfirmed(x, y)

		case GameEntityA.MoveRejected(x, y) if sender == location =>
			moveRejected(x, y)

		case GameEntityA.ListenChat(from, msg) if sender == location =>
			listenChat(from, msg)

		case GameEntityA.Listen(from, msg) if sender == location =>
			listen(from, msg)

		case msg =>
			handleMessage(msg)
	}

	def locationEntered(newLocation: ActorRef) {
		location ! LocationA.Leave
		location = newLocation
	}

	def moveConfirmed(x: Double, y: Double) {
		entity.x = x
		entity.y = y
	}

	def moveRejected(x: Double, y: Double) {
		entity.x = x
		entity.y = y
	}

	def listenChat(from: GameEntity, msg: String) {}

	def listen(from: GameEntity, msg: String) {}

	def handleMessage: Receive = { case _ => }

	def chat(msg: String) { location ! LocationA.BroadcastChat(msg) }

	def say(msg: String, radius: Double) { location ! LocationA.Broadcast(msg, radius) }
	
	def move(x: Double, y: Double) { location ! LocationA.MoveEntity(x, y) }
	
	def enterLocation(name: String) { context.actorSelection("/user/" + name) ! LocationA.Enter(entity) }
}