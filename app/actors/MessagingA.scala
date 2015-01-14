package actors

import akka.actor.Actor

class MessagingA extends Actor {
	override def receive = {
		case LocationA.BroadcastChat(entity, msg) =>
			blockingGet[LocationA.LookupEntitiesResult](context.parent, LocationA.LookupEntities(entity.x, entity.y, -1, null)) foreach {
				_.entities.values foreach { _ ! GameEntityA.ListenChat(entity, msg) }
			}

		case LocationA.Broadcast(entity, msg, radius) =>
			blockingGet[LocationA.LookupEntitiesResult](context.parent, LocationA.LookupEntities(entity.x, entity.y, radius, null)) foreach {
				_.entities.values foreach { _ ! GameEntityA.Listen(entity, msg) }
			}
	}
}
