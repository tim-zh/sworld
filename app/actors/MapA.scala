package actors

import akka.actor.{Actor, ActorRef}
import tiled.core.TileLayer
import tiled.io.TMXMapReader

class MapA(tiledMapFile: String, dao: ActorRef) extends Actor {
	var map: tiled.core.Map = _

	override def preStart() {
		val mapReader: TMXMapReader = new TMXMapReader
		//map = mapReader.readMap(tiledMapFile) todo tiled can only read xml in java
	}

	def receive = {
		case LocationA.MoveUser(user, x, y) =>
			if (0 <= x && x <= 500 && 0 <= y && y <= 500 && Math.abs(user.xy._1 - x) <= 10 && Math.abs(user.xy._2 - y) <= 10) {
				dao ! DaoA.UpdateUserPosition(user)
				sender ! PlayerA.MoveConfirmed(x, y)
			} else
				sender ! PlayerA.MoveRejected(user.xy._1, user.xy._2)
	}

	def isTileBlock(x: Double, y: Double) = {
		val properties = map.iterator().next().asInstanceOf[TileLayer].
				getTileInstancePropertiesAt(x.asInstanceOf[Int] / map.getTileWidth, y.asInstanceOf[Int] / map.getTileHeight)
		false//properties.contains("block?")
	}
}
