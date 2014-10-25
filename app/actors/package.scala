import models.User

package object actors {

  case class Say(user: User, msg: String)

  case class ChatMessage(user: User, msg: String)

  case class EnterLocation(user: User)
  object ConfirmEnterLocation
  object LeaveLocation

  case class Move(user: User, x: Double, y: Double)
  case class ConfirmMove(x: Double, y: Double)


  def filterNearbyUsers(x: Double, y: Double, users: Iterable[User], radius: Double) = users filter { user =>
    checkDistance(getDistance(x, y, user.position.x, user.position.y), radius)
  }

  def filterNearbyUsers(user: User, users: Iterable[User], radius: Double): Iterable[User] =
    filterNearbyUsers(user.position.x, user.position.y, users, radius)

  def getDistance(x0: Double, y0: Double, x1: Double, y1: Double) = (x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1)

  def checkDistance(distance: Double, radius: Double) = distance * distance <= radius * radius
}
