package actors

import akka.actor.Actor
import models.User

object PlayerA {

	object EnterLocation

	case class ConfirmMove(x: Double, y: Double)
	case class RejectMove(x: Double, y: Double)

	case class Say(user: User, msg: String)
	case class Chat(user: User, msg: String)
}

trait PlayerA extends Actor