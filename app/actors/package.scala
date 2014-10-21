import models.User

package object actors {
  case class ChatMessage(user: User, msg: String)

  object Subscribe

  object EnterLocation

  object LeaveLocation
}
