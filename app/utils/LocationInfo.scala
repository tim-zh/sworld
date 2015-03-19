package utils

import java.io.FileNotFoundException

import play.api.libs.json.{Json, JsValue, Writes}

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
			"map" -> Json.parse(info.map)
		)
	}
}

case class LocationInfo private(name: String, width: Int, height: Int, cellSize: Int) {
	val map = try
		Source.fromFile("public/maps/" + name + ".json").mkString
	catch {
		case e: FileNotFoundException =>
			""
	}
}
