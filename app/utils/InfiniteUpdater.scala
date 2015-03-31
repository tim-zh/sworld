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
	private var enabled = false
	private var registry = Map[ActorRef, Updatee]()
	private val timestamps = mutable.Map[ActorRef, Long]()

	def register(owner: ActorRef, updatee: Updatee) {
		registry += (owner -> updatee)
		timestamps += (owner -> System.currentTimeMillis)
		if (!enabled)
			start()
	}

	def remove(key: ActorRef) {
		registry -= key
		if (registry.isEmpty)
			enabled = false
	}

	def start() = Future {
		enabled = true
		while (enabled) {
			registry foreach { actorUpdatee =>
				val timestamp = timestamps(actorUpdatee._1)
				val dt = Math.min(System.currentTimeMillis - timestamp, 40)
				timestamps.update(actorUpdatee._1, System.currentTimeMillis)
				actorUpdatee._2.update(dt)
			}
			if (interval > 0)
				Thread.sleep(interval)
		}
	}
}
