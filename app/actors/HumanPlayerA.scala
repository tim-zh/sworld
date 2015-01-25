package actors

import akka.actor._
import models.GameEntity
import play.api.libs.json.{JsBoolean, JsObject, Json}

import scala.collection.mutable

class HumanPlayerA(out: ActorRef, initialLocation: ActorRef, entity: GameEntity) extends GameEntityA(initialLocation, entity) {

	override def preStart() {
		super.preStart()
		out ! Json.obj("move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
	}

	override def locationEntered(newLocation: ActorRef) {
		super.locationEntered(newLocation)
		out ! Json.obj("newLocation" -> newLocation.path.name, "move" -> Json.obj("x" -> entity.x, "y" -> entity.y))
	}

	override def lookAround(entities: mutable.Map[GameEntity, ActorRef], oldEntities: mutable.Map[GameEntity, ActorRef]) {
		val entitiesMap = entities.map(e => (e._1.id, e._1))
		val oldEntitiesMap = oldEntities.map(e => (e._1.id, e._1))

		val ids = entitiesMap.keySet
		val oldIds = oldEntitiesMap.keySet

		val newIds = ids.diff(oldIds).filter(_ != entity.id)
		val goneIds = oldIds.diff(ids)
		val changedIds = ids.diff(newIds).filter(id =>
			id != entity.id && (Math.abs(entitiesMap(id).x - oldEntitiesMap(id).x) >= 1 || Math.abs(entitiesMap(id).y - oldEntitiesMap(id).y) >= 1))
		val newEntitiesArr = Json.toJson(newIds map { id =>
			val e = entitiesMap(id)
			Json.obj("id" -> id, "x" -> e.x, "y" -> e.y, "type" -> e.eType)
		})
		val goneEntitiesArr = Json.toJson(goneIds map { id =>
			val e = oldEntitiesMap(id)
			Json.obj("id" -> id, "x" -> e.x, "y" -> e.y, "type" -> e.eType)
		})
		val changedEntitiesArr = Json.toJson(changedIds map { id =>
			val e = entitiesMap(id)
			Json.obj("id" -> id, "x" -> e.x, "y" -> e.y)
		})

		out ! Json.obj("newEntities" -> newEntitiesArr, "changedEntities" -> changedEntitiesArr, "goneEntities" -> goneEntitiesArr)
	}

	override def moveRejected(x: Double, y: Double) {
		super.moveRejected(x, y)
		out ! Json.obj("move" -> Json.obj("x" -> x, "y" -> y))
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
			move(newX, newY)

		case jsObj: JsObject if jsObj.value contains "chat" =>
			val msg = (jsObj \ "chat").as[String]
			chat(msg)

		case jsObj: JsObject if jsObj.value contains "say" =>
			val msg = (jsObj \ "say").as[String]
			say(msg, 50)
			if (msg == "rise")
				createGameEntity(GameEntity(generateId(), true, "bot", "bot", entity.location, entity.x + 30, entity.y + 30, 100, 50))
	}
}