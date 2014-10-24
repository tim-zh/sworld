import models.User

package object actors {

  case class ChatMessage(user: User, msg: String)

  object Subscribe

  object EnterLocation

  object ConfirmEnterLocation

  object LeaveLocation

  case class Move(user: User, x: Double, y: Double)

  case class ConfirmMove(x: Double, y: Double)

}
