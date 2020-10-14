package restui.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import restui.Configuration
import restui.models.{Metadata, Service, ServiceEvent}
import restui.providers.ProvidersLoader
import restui.server.http.HttpServer
import restui.server.service._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

// $COVERAGE-OFF$
object Main extends App with LazyLogging {
  private val Namespace: String                   = "restui.http"
  private val config                              = Configuration.config(args.headOption)
  implicit val system: ActorSystem[_]             = ActorSystem(Behaviors.empty, "restui")
  implicit val executionContext: ExecutionContext = system.executionContext

  val interface                    = config.getString(s"$Namespace.interface")
  val port                         = config.getInt(s"$Namespace.port")
  val selfSpecification            = config.getBoolean("restui.self-specification")
  private val (queue, eventSource) = EventSource.createEventSource.run()
  private val serviceActor =
    system.systemActorOf(ServiceActor(queue), "serviceActor")
  private val httpServer = new HttpServer(serviceActor, eventSource)

  val specificationSource = if (selfSpecification) {
    val specification = scala.io.Source
      .fromResource("specification.yaml")
      .getLines()
      .mkString("\n")
    Source.single(
      Main.getClass.getCanonicalName -> ServiceEvent.ServiceUp(
        Service.OpenApi("restui:restui",
                        "RestUI",
                        specification,
                        Map(Metadata.File -> "specification.yaml"))))
  } else Source.empty[(String, ServiceEvent)]

  ProvidersLoader
    .load(config)
    .prepend(specificationSource)
    .runWith(Sink.foreach { case (provider, event) =>
      serviceActor ! ServiceActor.Add(provider, event)
    })

  httpServer.bind(interface, port).onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      logger.info(
        s"Server online at http://${address.getHostName}:${address.getPort}/")
      sys.addShutdownHook {
        binding.terminate(hardDeadline = 3.seconds).flatMap { _ =>
          system.terminate()
          system.whenTerminated
        }
      }
    case Failure(ex) =>
      logger.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.whenTerminated.onComplete(_ => sys.exit(1))
  }

}