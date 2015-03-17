package actors

import akka.actor._
import models.GameEntity
import play.api.libs.json.{JsBoolean, JsObject, Json}

class HumanPlayerA(out: ActorRef, initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {

	var lastMoveTimestamp = 0L

	override def locationEntered(newLocation: ActorRef, entities: Map[GameEntity, ActorRef]) {
		super.locationEntered(newLocation, entities)
		out ! Json.obj(
			"newLocation" -> newLocation.path.name,
			"move" -> Json.obj("x" -> entity.x, "y" -> entity.y),
			"eNew" -> Json.toJson(entities.keys map { e =>
				Json.obj("id" -> e.id, "x" -> Math.floor(e.x), "y" -> Math.floor(e.y), "type" -> e.eType)
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
			"id" -> e.id, "x" -> Math.floor(e.x), "y" -> Math.floor(e.y), "type" -> e.eType
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
		var message = Json.obj("say" -> msg, "user" -> from.name)
		if (from.id == entity.id)
			message = message + ("isOwner" -> JsBoolean(true))
		out ! message
	}

	override def handleMessage: Receive = {
		case jsObj: JsObject =>
			if (jsObj.value contains "newLocation") {
				val name = (jsObj \ "newLocation").as[String]
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
					createGameEntity(GameEntity(GameEntityA.generateId(), true, "bot", "bot", entity.location, entity.x + 30, entity.y + 30, 100, 100, 15))
			}
	}
}