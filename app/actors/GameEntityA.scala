package actors

import akka.actor.{ActorRef, Cancellable}
import models.GameEntity

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object GameEntityA {

	object LocationEntered

	case class Listen(from: GameEntity, msg: String)
	case class ListenChat(from: GameEntity, msg: String)

	private object LookAround

	private var lastTransientId: Long = -1

	def generateId() = synchronized { lastTransientId -= 1; lastTransientId }
}

abstract class GameEntityA(var location: ActorRef, entity: GameEntity) extends ReceiveLoggerA {
	import GameEntityA._

	private var lookAroundTick: Cancellable = null
	private var visibleEntitiesMap = Map[GameEntity, ActorRef]()
	private var lastMoveTime: Long = _

	override def preStart() {
		location ! LocationA.Enter(entity.copy())
		lastMoveTime = System.currentTimeMillis()
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
			val newEntities = entities.filter(_._1.id != entity.id)
			lookAround(newEntities, visibleEntitiesMap)
			visibleEntitiesMap = newEntities
			context.system.scheduler.scheduleOnce(100 milliseconds, self, LookAround)

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
		entity.location = location.path.name
	}

	def lookAround(entities: Map[GameEntity, ActorRef], oldEntities: Map[GameEntity, ActorRef]) {}

	def listenChat(from: GameEntity, msg: String) {}

	def listen(from: GameEntity, msg: String) {}

	def handleMessage: Receive = { case _ => }

	def chat(msg: String) { location ! LocationA.BroadcastChat(msg) }

	def say(msg: String, radius: Double) { location ! LocationA.Broadcast(msg, radius) }

	def moveTo(x: Double, y: Double): Boolean = {
		val dt = System.currentTimeMillis() - lastMoveTime
		lastMoveTime += dt
		val k = dt * entity.maxSpeed / 1000
		var (dx, dy) = getVelocityVectorTo(x, y)
		dx *= k
		dy *= k
		val newX = if (Math.abs(dx) >= Math.abs(x - entity.x)) x else entity.x + dx
		val newY = if (Math.abs(dy) >= Math.abs(y - entity.y)) y else entity.y + dy
		move(newX, newY)
	}

	def move(x: Double, y: Double): Boolean = {
		if (!isMoveAllowed(x, y))
			return false
		entity.dx = x - entity.x
		entity.dy = y - entity.y
		entity.x = x
		entity.y = y
		location ! LocationA.MoveEntity(x, y)
		true
	}

	def enterLocation(name: String) { context.actorSelection("/user/" + name) ! LocationA.Enter(entity.copy()) }

	def createGameEntity(entity: GameEntity) = entity.name match {
		case "bot" =>
			location ! LocationA.CreateEntity(classOf[BotPlayerA], entity)
	}

	def getNewGoneRest(entities: Map[GameEntity, ActorRef], oldEntities: Map[GameEntity, ActorRef]) = {
		val entitiesMap = entities.map(e => (e._1.id, e._1))
		val oldEntitiesMap = oldEntities.map(e => (e._1.id, e._1))

		val ids = entitiesMap.keySet
		val oldIds = oldEntitiesMap.keySet

		val newIds = ids.diff(oldIds)
		val goneIds = oldIds.diff(ids)
		val restIds = ids.diff(newIds)

		(newIds.map(entitiesMap(_)), goneIds.map(oldEntitiesMap(_)), restIds.map(entitiesMap(_)))
	}

	def getVelocityVectorTo(x: Double, y: Double): (Double, Double) = {
		val (dx, dy) = (x - entity.x, y - entity.y)
		if (dx == 0 && dy == 0)
			return (0, 0)
		val k = entity.maxSpeed / Math.hypot(dx, dy)
		(dx * k, dy * k)
	}

	def isMoveAllowed(x: Double, y: Double) =
		0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.hypot(entity.x - x, entity.y - y) <= entity.maxSpeed * 1.5
}