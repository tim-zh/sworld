package actors

import akka.actor.{Actor, ActorRef}
import tiled.core.{MapLayer, ObjectGroup}
import tiled.io.TMXMapReader
import scala.collection.JavaConversions._

class MapA(tiledMapFile: String, dao: ActorRef) extends Actor {
	var map: tiled.core.Map = _

	override def preStart() {
		val mapReader: TMXMapReader = new TMXMapReader
		mapReader.setLoadTilesets(false)
		map = mapReader.readMap(tiledMapFile)
	}

	def receive = {
		case LocationA.MoveEntity(entity, x, y) =>
			if (0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(entity.x - x) <= 10 && Math.abs(entity.y - y) <= 10) {
				dao ! DaoA.UpdateEntityPosition(entity)
				sender ! GameEntityA.MoveConfirmed(x, y)
			} else
				sender ! GameEntityA.MoveRejected(entity.x, entity.y)
	}

	def isTileBlock(x: Double, y: Double): Boolean = {
		for (group: MapLayer <- map.iterator() if group.isInstanceOf[ObjectGroup])
			if (group.asInstanceOf[ObjectGroup].getObjectAt(x.asInstanceOf[Int] / map.getTileWidth, y.asInstanceOf[Int] / map.getTileHeight) != null)
				return true
		false
	}
}
