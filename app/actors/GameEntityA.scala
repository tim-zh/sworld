package actors

import akka.actor.{Cancellable, Actor, ActorRef}
import models.GameEntity

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object GameEntityA {

	object LocationEntered

	case class MoveConfirmed(x: Double, y: Double)
	case class MoveRejected(x: Double, y: Double)

	case class Listen(from: GameEntity, msg: String)
	case class ListenChat(from: GameEntity, msg: String)

	private object LookAround
}

abstract class GameEntityA(var location: ActorRef, entity: GameEntity) extends Actor {
	import GameEntityA._

	private var lastTransientId: Long = -1
	private var lookAroundTick: Cancellable = null
	private var visibleEntitiesMap = mutable.Map[GameEntity, ActorRef]()

	protected def generateId() = synchronized { lastTransientId -= 1; lastTransientId }

	override def preStart() {
		location ! LocationA.Enter(entity)
	}

	override def receive = {
		case LocationEntered =>
			if (lookAroundTick != null)
				lookAroundTick.cancel()
			locationEntered(sender)
			lookAroundTick = context.system.scheduler.scheduleOnce(100 milliseconds, self, LookAround)

		case LookAround =>
			location ! LocationA.LookupEntities(entity.x, entity.y, entity.viewRadius, null)

		case LocationA.LookupEntitiesResult(entities, param) =>
			lookAround(entities, visibleEntitiesMap)
			visibleEntitiesMap = entities
			context.system.scheduler.scheduleOnce(100 milliseconds, self, LookAround)

		case MoveConfirmed(x, y) if sender == location =>
			moveConfirmed(x, y)

		case MoveRejected(x, y) if sender == location =>
			moveRejected(x, y)

		case ListenChat(from, msg) if sender == location =>
			listenChat(from, msg)

		case Listen(from, msg) if sender == location =>
			listen(from, msg)

		case msg =>
			handleMessage(msg)
	}

	def locationEntered(newLocation: ActorRef) {
		if (location != newLocation)
			location ! LocationA.Leave
		location = newLocation
	}

	def lookAround(entities: mutable.Map[GameEntity, ActorRef], oldEntities: mutable.Map[GameEntity, ActorRef]) {}

	def moveConfirmed(x: Double, y: Double) {
		entity.dx = x - entity.x
		entity.dy = y - entity.y
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

	def createGameEntity(entity: GameEntity) = entity.name match {
		case "bot" =>
			location ! LocationA.CreateEntity(classOf[BotPlayerA], entity)
	}

	def getNewGoneRest(entities: mutable.Map[GameEntity, ActorRef], oldEntities: mutable.Map[GameEntity, ActorRef]) = {
		val entitiesMap = entities.map(e => (e._1.id, e._1))
		val oldEntitiesMap = oldEntities.map(e => (e._1.id, e._1))

		val ids = entitiesMap.keySet
		val oldIds = oldEntitiesMap.keySet

		val newIds = ids.diff(oldIds).filter(_ != entity.id)
		val goneIds = oldIds.diff(ids)
		val restIds = ids.diff(newIds).filter(id => id != entity.id)

		(newIds.map(entitiesMap(_)), goneIds.map(oldEntitiesMap(_)), restIds.map(entitiesMap(_)))
	}

	def getVelocityVectorTo(x: Double, y: Double) = {
		val (dx, dy) = (x - entity.x, y - entity.y)
		val k = entity.maxSpeed / Math.hypot(dx, dy)
		(dx * k, dy * k)
	}
}