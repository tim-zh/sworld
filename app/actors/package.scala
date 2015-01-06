import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent._
import scala.concurrent.duration.{FiniteDuration, _}

package object actors {

	def blockingGet[R](from: ActorRef, msg: AnyRef, timeout: FiniteDuration = 1 second): Option[R] = {
		import scala.concurrent.ExecutionContext.Implicits.global
		try {
			implicit val askTimeout: Timeout = Timeout(timeout)
			Some(Await.result(from ? msg map {
				_.asInstanceOf[R]
			}, timeout))
		} catch {
			case e: TimeoutException => None
		}
	}
}
