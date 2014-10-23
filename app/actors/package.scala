import models.User

package object actors {

  case class ChatMessage(user: User, msg: String)

  object Subscribe

  object EnterLocation

  object ConfirmEnterLocation

  object LeaveLocation

  case class Move(x: Double, y: Double)

  case class ConfirmMove(x: Double, y: Double)

}
