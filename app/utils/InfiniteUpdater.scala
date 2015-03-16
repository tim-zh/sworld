package utils

import akka.actor.ActorRef

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Updatee {
	def update(): Unit
}

object InfiniteUpdater {
	var interval: Int = 0
	private var enabled = false
	private var registry = Map[ActorRef, Updatee]()

	def register(owner: ActorRef, updatee: Updatee) {
		registry += (owner -> updatee)
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
			registry.foreach(_._2.update())
			if (interval > 0)
				Thread.sleep(interval)
		}
	}
}
