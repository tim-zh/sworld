package actors

import akka.actor._
import models.GameEntity
import play.api.libs.json.{JsBoolean, JsObject, Json}

class HumanPlayerA(out: ActorRef, initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {

	override def preStart() {
		super.preStart()
		out ! Json.obj("move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
	}

	override def locationEntered(newLocation: ActorRef) {
		super.locationEntered(newLocation)
		out ! Json.obj("newLocation" -> newLocation.path.name, "move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
	}

	override def lookAround(entities: Map[GameEntity, ActorRef], oldEntities: Map[GameEntity, ActorRef]) {
		if (entities.isEmpty && oldEntities.isEmpty)
			return
		val oldEntitiesMap = oldEntities.map(e => (e._1.id, e._1))
		val (newEntities, goneEntities, restEntities) = getNewGoneRest(entities, oldEntities)
		val (changedEntities, unchangedEntities) = restEntities.partition { e =>
			Math.abs(e.x - oldEntitiesMap(e.id).x) >= 1 || Math.abs(e.y - oldEntitiesMap(e.id).y) >= 1 }

		val newEntitiesArr = Json.toJson(newEntities map { e =>
			Json.obj("id" -> e.id, "x" -> e.x, "y" -> e.y, "type" -> e.eType)
		})
		val goneEntitiesArr = Json.toJson(goneEntities map { e =>
			Json.obj("id" -> e.id, "x" -> e.x, "y" -> e.y, "type" -> e.eType)
		})
		val changedEntitiesArr = Json.toJson(changedEntities.map { e =>
			Json.obj("id" -> e.id, "x" -> e.x, "y" -> e.y)
		})
		val unchangedEntitiesArr = Json.toJson(unchangedEntities.map { e =>
			Json.obj("id" -> e.id)
		})

		if (newEntities.nonEmpty || goneEntities.nonEmpty || changedEntities.nonEmpty || unchangedEntities.nonEmpty)
			out ! Json.obj(
				"newEntities" -> newEntitiesArr,
				"changedEntities" -> changedEntitiesArr,
				"goneEntities" -> goneEntitiesArr,
				"unchangedEntities" -> unchangedEntitiesArr)
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
		case jsObj: JsObject if jsObj.value contains "newLocation" =>
			val name = (jsObj \ "newLocation").as[String]
			enterLocation(name)

		case jsObj: JsObject if jsObj.value contains "move" =>
			val newX = (jsObj \ "move" \ "x").as[Double]
			val newY = (jsObj \ "move" \ "y").as[Double]
			if (!moveTo(newX, newY))
				out ! Json.obj("move" -> Json.obj("x" -> entity.x, "y" -> entity.y))

		case jsObj: JsObject if jsObj.value contains "chat" =>
			val msg = (jsObj \ "chat").as[String]
			chat(msg)

		case jsObj: JsObject if jsObj.value contains "say" =>
			val msg = (jsObj \ "say").as[String]
			say(msg, 50)
			if (msg == "rise")
				createGameEntity(GameEntity(GameEntityA.generateId(), true, "bot", "bot", entity.location, entity.x + 30, entity.y + 30, 100, 10))
	}
}