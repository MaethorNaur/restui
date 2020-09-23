package restui.server.service

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.scaladsl.SourceQueueWithComplete
import restui.models._
import restui.specifications.Validator

class ServiceActor(queue: SourceQueueWithComplete[Event]) extends Actor with ActorLogging {
  import ServiceActor._
  override def receive: Receive = handleReceive(Map.empty)

  private def handleReceive(services: Map[String, Service]): Receive = {
    case (provider: String, ServiceEvent.ServiceUp(service)) if !Validator.isValid(service.file) =>
      log.debug(s"Invalid specification from $provider")
      sender() ! Ack
    case (provider: String, ServiceEvent.ServiceUp(service)) =>
      log.debug("{} got a new service", provider)
      val serviceWithHash    = computeSha1(service)
      val serviceNameChanged = hasServiceNameChanged(services, serviceWithHash)

      if (serviceNameChanged)
        queue.offer(Event.ServiceDown(serviceWithHash.id))

      if (isNewService(services, serviceWithHash) || serviceNameChanged)
        queue.offer(Event.ServiceUp(serviceWithHash.id, serviceWithHash.name, serviceWithHash.metadata))

      if (hasContentChanged(services, serviceWithHash))
        queue.offer(Event.ServiceContentChanged(serviceWithHash.id))

      context.become(handleReceive(services + (serviceWithHash.id -> serviceWithHash)))
      sender() ! Ack

    case (provider: String, ServiceEvent.ServiceDown(serviceId)) =>
      queue.offer(Event.ServiceDown(serviceId))
      log.debug("{} removed a service", provider)
      context.become(handleReceive(services - serviceId))
      sender() ! Ack

    case Get(serviceId) => sender() ! services.get(serviceId)
    case GetAll         => sender() ! services.values.toList
    case Init           => sender() ! Ack
    case Complete       => sender() ! Ack
  }

  private def hasServiceNameChanged(services: Map[String, Service], service: Service): Boolean =
    services.exists {
      case (id, currentService) =>
        id == service.id && currentService.name != service.name
    }

  private def computeSha1(service: Service): Service = {
    val md       = java.security.MessageDigest.getInstance("SHA-1")
    val sha1Hash = md.digest(service.file.getBytes("UTF-8")).map("%02x".format(_)).mkString
    service.copy(hash = sha1Hash)
  }

  private def isNewService(services: Map[String, Service], service: Service): Boolean = !services.contains(service.id)

  private def hasContentChanged(services: Map[String, Service], service: Service): Boolean =
    services.exists {
      case (id, Service(_, _, _, _, hash)) => id == service.id && hash != service.hash
    }
}

object ServiceActor {
  def props(queue: SourceQueueWithComplete[Event]): Props = Props(classOf[ServiceActor], queue)
  case class Get(serviceId: String)
  case object GetAll
  case object Init
  case object Complete
  case object Ack
}
