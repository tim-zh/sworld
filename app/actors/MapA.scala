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
		case LocationA.MoveUser(user, x, y) =>
			if (0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(user.xy._1 - x) <= 10 && Math.abs(user.xy._2 - y) <= 10) {
				dao ! DaoA.UpdateUserPosition(user)
				sender ! PlayerA.MoveConfirmed(x, y)
			} else
				sender ! PlayerA.MoveRejected(user.xy._1, user.xy._2)
	}

	def isTileBlock(x: Double, y: Double): Boolean = {
    for (group: MapLayer <- map.iterator() if group.isInstanceOf[ObjectGroup])
      if (group.asInstanceOf[ObjectGroup].getObjectAt(x.asInstanceOf[Int] / map.getTileWidth, y.asInstanceOf[Int] / map.getTileHeight) != null)
        return true
		false
	}
}
