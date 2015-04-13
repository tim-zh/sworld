package utils

import akka.actor.ActorRef

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Updatee {
	def update(dt: Long): Unit
}

object InfiniteUpdater {
	var interval: Int = 10
	private var registry = Map[ActorRef, Updatee]()
	private val timestamps = mutable.Map[ActorRef, Long]()
	private var toRemove = Seq[ActorRef]()

	def register(owner: ActorRef, updatee: Updatee) {
		registry += (owner -> updatee)
		timestamps += (owner -> System.currentTimeMillis)
	}

	def remove(key: ActorRef): Unit = InfiniteUpdater.synchronized {
		toRemove +:= key
	}

	Future {
		while (true) {
			registry foreach { actorUpdatee =>
				if (! toRemove.contains(actorUpdatee._1)) {
					val timestamp = timestamps(actorUpdatee._1)
					val dt = Math.min(System.currentTimeMillis - timestamp, 40)
					timestamps.update(actorUpdatee._1, System.currentTimeMillis)
					actorUpdatee._2.update(dt)
				}
			}
			InfiniteUpdater.synchronized {
				toRemove foreach { key =>
					registry -= key
					timestamps -= key
				}
				toRemove = Seq[ActorRef]()
			}
			if (interval > 0)
				Thread.sleep(interval)
		}
	} onFailure {
		case e =>
			e.printStackTrace()
	}
}
