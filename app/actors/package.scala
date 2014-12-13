import models.User

package object actors {

  case class Say(user: User, msg: String)

  case class ChatMessage(user: User, msg: String)

  case class EnterLocation(user: User)
  object ConfirmEnterLocation
  object LeaveLocation

  case class Move(user: User, x: Double, y: Double)
  case class ConfirmMove(x: Double, y: Double)
  case class RejectMove(x: Double, y: Double)


  def filterNearbyUsers(xy: (Double, Double), users: Iterable[User], radius: Double) = users filter { user =>
    getDistance(xy, user.xy) <= radius
  }

  def filterNearbyUsers(user: User, users: Iterable[User], radius: Double): Iterable[User] =
    filterNearbyUsers(user.xy, users, radius)

  def getDistance(xy0: (Double, Double), xy1: (Double, Double)) =
    Math.sqrt((xy0._1 - xy1._1) * (xy0._1 - xy1._1) + (xy0._2 - xy1._2) * (xy0._2 - xy1._2))
}
