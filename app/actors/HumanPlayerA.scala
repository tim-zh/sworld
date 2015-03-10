package actors

import akka.actor._
import models.GameEntity
import play.api.libs.json.{JsBoolean, JsObject, Json}

class HumanPlayerA(out: ActorRef, initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {

	var lastMoveTimestamp = 0L

	override def locationEntered(newLocation: ActorRef) {
		super.locationEntered(newLocation)
		out ! Json.obj("newLocation" -> newLocation.path.name, "move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
	}

	override def lookAround(entities: Map[GameEntity, ActorRef], oldEntities: Map[GameEntity, ActorRef]) {
		if (entities.isEmpty && oldEntities.isEmpty)
			return
		val oldEntitiesMap = oldEntities.map(e => (e._1.id, e._1))
		val (newEntities, goneEntities, restEntities) = getNewGoneRest(entities, oldEntities)

		val newEntitiesArr = Json.toJson(newEntities map { e =>
			Json.obj("id" -> e.id, "x" -> e.x, "y" -> e.y, "type" -> e.eType, "maxSpeed" -> e.maxSpeed)
		})
		val goneEntitiesArr = Json.toJson(goneEntities map { e =>
			Json.obj("id" -> e.id)
		})
		val changedEntitiesArr = Json.toJson(restEntities.map { e =>
			Json.obj("id" -> e.id, "x" -> e.x, "y" -> e.y, "dx" -> e.dx, "dy" -> e.dy)
		})

		out ! Json.obj(
			"newEntities" -> newEntitiesArr,
			"changedEntities" -> changedEntitiesArr,
			"goneEntities" -> goneEntitiesArr)
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
				val dt = System.currentTimeMillis() - lastMoveTimestamp
				lastMoveTimestamp = System.currentTimeMillis()
				if (isMoveAllowed(newX, newY, dt))
					setPositionAndVelocity(newX, newY)
				else
					out ! Json.obj("move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
			}

			if (jsObj.value contains "chat") {
				val msg = (jsObj \ "chat").as[String]
				chat(msg)
			}

			if (jsObj.value contains "say") {
				val msg = (jsObj \ "say").as[String]
				say(msg, 50)
				if (msg == "rise")
					createGameEntity(GameEntity(GameEntityA.generateId(), true, "bot", "bot", entity.location, entity.x + 30, entity.y + 30, 100, 15))
			}
	}
}