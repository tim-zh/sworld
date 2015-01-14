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

		case GameEntityA.MoveConfirmed(x, y) =>
			moveConfirmed(x, y)

		case GameEntityA.MoveRejected(x, y) =>
			moveRejected(x, y)

		case GameEntityA.ListenChat(from, msg) =>
			listenChat(from, msg)

		case GameEntityA.Listen(from, msg) =>
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

	def chat(msg: String) { location ! LocationA.BroadcastChat(entity, msg) }

	def say(msg: String, radius: Double) { location ! LocationA.Broadcast(entity, msg, radius) }
	
	def move(x: Double, y: Double) { location ! LocationA.MoveEntity(entity, x, y) }
	
	def enterLocation(name: String) { context.actorSelection("/user/" + name) ! LocationA.Enter(entity) }
}