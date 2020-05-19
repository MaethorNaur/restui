package restui.server.service

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.scaladsl.SourceQueueWithComplete
import restui.server.http.{Models => HttpModels}
import restui.servicediscovery.Models._

class EndpointsActor(queue: SourceQueueWithComplete[HttpModels.Event]) extends Actor with ActorLogging {
  import EndpointsActor._
  override def receive: Receive = handleReceive(Map.empty)

  private def handleReceive(endpoints: Map[String, Endpoint]): Receive = {
    case (provider: String, Up(endpoint)) =>
      log.debug("{} got a new endpoint", provider)
      queue.offer(HttpModels.Up(endpoint.serviceName))
      context.become(handleReceive(endpoints + (endpoint.serviceName -> endpoint)))

    case (provider: String, Down(endpoint)) =>
      queue.offer(HttpModels.Down(endpoint.serviceName))
      log.debug("{} removed an endpoint", provider)
      context.become(handleReceive(endpoints - endpoint.serviceName))

    case Get(serviceName) => sender() ! endpoints.get(serviceName)
    case GetAll           => sender() ! endpoints.values.toList
    case message          => log.warning("Unmatch {}", message)
  }

}

object EndpointsActor {
  def props(queue: SourceQueueWithComplete[HttpModels.Event]): Props = Props(classOf[EndpointsActor], queue)
  case class Get(serviceName: String)
  case object GetAll
}