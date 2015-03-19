package utils

import java.io.FileNotFoundException

import play.api.libs.json.{JsString, Json, JsValue, Writes}

import scala.io.Source

object LocationInfo {
	val default = LocationInfo("default", 50, 18, 32)

	val default2 = LocationInfo("default2", 50, 18, 32)

	implicit val locationInfoWrites = new Writes[LocationInfo] {
		override def writes(info: LocationInfo): JsValue = Json.obj(
			"name" -> info.name,
			"width" -> info.width,
			"height" -> info.height,
			"cellSize" -> info.cellSize,
			"map" -> info.map
		)
	}
}

case class LocationInfo private(name: String, width: Int, height: Int, cellSize: Int) {
	val map = try
		Json.parse(Source.fromFile("public/maps/" + name + ".json").mkString)
	catch {
		case e: FileNotFoundException =>
			JsString("")
	}

	private val cells: Array[Array[Short]] = ((map \ "layers")(0) \ "data").as[Array[Short]].grouped(width).toArray

	def getCellType(cellXIndex: Int, cellYIndex: Int): Option[Short] =
		if (cellYIndex >= 0 && cellYIndex < cells.length && cellXIndex >= 0 && cellXIndex < cells(cellYIndex).length)
			Some(cells(cellYIndex)(cellXIndex))
		else
			None

	def getCellType(x: Double, y: Double): Option[Short] = getCellType(x.toInt / cellSize, y.toInt / cellSize)
}
