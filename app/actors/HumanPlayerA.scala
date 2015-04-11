package actors

import akka.actor._
import models.{EntityType, GameEntity}
import play.api.libs.json.{JsBoolean, JsObject, Json}
import utils.{Utils, LocationInfo}

class HumanPlayerA(out: ActorRef, initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {

	var lastMoveTimestamp = 0L

	override def locationEntered(newLocation: ActorRef, info: LocationInfo, entities: Set[GameEntity]) {
		super.locationEntered(newLocation, info, entities)
		import LocationInfo.locationInfoWrites
		out ! Json.obj(
			"location" -> Json.toJson(info),
			"move" -> Json.obj("x" -> entity.x, "y" -> entity.y),
			"eNew" -> Json.toJson(entities map { e =>
				Json.obj("id" -> e.id, "x" -> Math.floor(e.x), "y" -> Math.floor(e.y), "type" -> e.eType.name)
			}),
			"eGone" -> Json.toJson(visibleEntities map { e =>
				Json.obj("id" -> e.id)
			})
		)
	}

	override def notifyUpdatedEntity(e: GameEntity) {
		out ! Json.obj("eUpdate" -> Json.arr(Json.obj(
			"id" -> e.id, "x" -> Math.floor(e.x), "y" -> Math.floor(e.y), "dx" -> e.dx, "dy" -> e.dy
		)))
	}

	override def notifyNewEntity(e: GameEntity) {
		out ! Json.obj("eNew" -> Json.arr(Json.obj(
			"id" -> e.id, "x" -> Math.floor(e.x), "y" -> Math.floor(e.y), "type" -> e.eType.name
		)))
	}

	override def notifyGoneEntity(e: GameEntity) {
		out ! Json.obj("eGone" -> Json.arr(Json.obj(
			"id" -> e.id
		)))
	}

	override def listenChat(from: GameEntity, msg: String) {
		var message = Json.obj("chat" -> msg, "user" -> from.name)
		if (from.id == entity.id)
			message = message + ("isOwner" -> JsBoolean(true))
		out ! message
	}

	override def listen(from: GameEntity, msg: String) {
		var message = Json.obj("id" -> from.id, "say" -> msg)
		if (from.id == entity.id)
			message = message + ("isOwner" -> JsBoolean(true))
		out ! message
	}

	override def handleMessage: Receive = {
		case jsObj: JsObject =>
			if (jsObj.value contains "location") {
				val name = (jsObj \ "location").as[String]
				enterLocation(name)
			}

			if (jsObj.value contains "move") {
				val newX = (jsObj \ "move" \ "x").as[Double]
				val newY = (jsObj \ "move" \ "y").as[Double]
				val stop = (jsObj \ "move" \ "stop").asOpt[Boolean]
				val dt = Math.min(System.currentTimeMillis() - lastMoveTimestamp, 40)
				lastMoveTimestamp = System.currentTimeMillis()
				if (isMoveAllowed(newX, newY, dt))
					setPositionAndVelocity(newX, newY, stop.getOrElse(false))
				else
					out ! Json.obj("move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
			}

			if (jsObj.value contains "chat") {
				val msg = (jsObj \ "chat").as[String]
				chat(msg)
			}

			if (jsObj.value contains "say") {
				val msg = (jsObj \ "say").as[String]
				say(msg)
				if (msg == "rise")
					createGameEntity(GameEntity(GameEntityA.generateId(), true, EntityType.bot, "bot", entity.location, entity.x + 30, entity.y + 30, 8, 100, 100, 15))
			}

			if (jsObj.value contains "mouseDown") {
				val x = (jsObj \ "mouseDown" \ "x").as[Int]
				val y = (jsObj \ "mouseDown" \ "y").as[Int]
				mouseDown(x, y)
			}
	}

	def mouseDown(x: Int, y: Int) {
		val (dx, dy) = Utils.getVelocityVectorTo(entity, x, y)
		createGameEntity(GameEntity(GameEntityA.generateId(), true, EntityType.grenade, "grenade", entity.location, entity.x, entity.y, 8, 100, 100, 15, dx, dy))
	}
}