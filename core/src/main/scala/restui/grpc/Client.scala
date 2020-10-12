package restui.grpc

import akka.actor.ClassicActorSystemProvider
import akka.grpc.GrpcClientSettings
import akka.grpc.internal.{
  ClientState,
  NettyClientUtils,
  ScalaBidirectionalStreamingRequestBuilder,
  ScalaClientStreamingRequestBuilder,
  ScalaServerStreamingRequestBuilder,
  ScalaUnaryRequestBuilder
}
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import io.circe.Json
import io.grpc.MethodDescriptor
import restui.protobuf.data._

import scala.concurrent.{ExecutionContext, Future}

object Client {
  type FutureJson = Future[Json]
  type SourceJson = Source[Json, NotUsed]
}

class Client(service: Service, settings: GrpcClientSettings)(implicit
    sys: ClassicActorSystemProvider) {
  import Client._
  private implicit val executionContext: ExecutionContext =
    sys.classicSystem.dispatcher
  private val clientState = new ClientState(
    settings,
    akka.event.Logging(sys.classicSystem, this.getClass))
  private val options = NettyClientUtils.callOptions(settings)

  def request(methodName: String, input: Json): Option[FutureJson] =
    service.methods.collectFirst {
      case method @ Method(name, _, _, false, false) if name == methodName =>
        val descriptor =
          methodDescriptor(MethodDescriptor.MethodType.UNARY, method)
        new ScalaUnaryRequestBuilder(descriptor,
                                     clientState.internalChannel,
                                     options,
                                     settings).invoke(input)
    }

  def request(methodName: String, source: SourceJson): Option[SourceJson] =
    service.methods.collectFirst {
      case method @ Method(name, _, _, true, true) if name == methodName =>
        val descriptor =
          methodDescriptor(MethodDescriptor.MethodType.BIDI_STREAMING, method)
        val fqName = s"${service.fullName}.$name"
        new ScalaBidirectionalStreamingRequestBuilder(
          descriptor,
          fqName,
          clientState.internalChannel,
          options,
          settings)
          .invoke(source)
    }

  def streamingRequest(methodName: String, input: Json): Option[SourceJson] =
    service.methods.collectFirst {
      case method @ Method(name, _, _, true, false) if name == methodName =>
        val descriptor =
          methodDescriptor(MethodDescriptor.MethodType.SERVER_STREAMING, method)
        val fqName = s"${service.fullName}.$name"
        new ScalaServerStreamingRequestBuilder(descriptor,
                                               fqName,
                                               clientState.internalChannel,
                                               options,
                                               settings)
          .invoke(input)
    }

  def streamingRequest(methodName: String,
                       source: SourceJson): Option[FutureJson] =
    service.methods.collectFirst {
      case method @ Method(name, _, _, false, true) if name == methodName =>
        val descriptor =
          methodDescriptor(MethodDescriptor.MethodType.CLIENT_STREAMING, method)
        val fqName = s"${service.fullName}.$name"
        new ScalaClientStreamingRequestBuilder(descriptor,
                                               fqName,
                                               clientState.internalChannel,
                                               options,
                                               settings)
          .invoke(source)
    }

  def close(): Future[Done] = clientState.close()

  private def methodDescriptor(`type`: MethodDescriptor.MethodType,
                               method: Method): MethodDescriptor[Json, Json] =
    MethodDescriptor
      .newBuilder()
      .setType(`type`)
      .setFullMethodName(
        MethodDescriptor.generateFullMethodName(service.fullName, method.name))
      .setRequestMarshaller(new Marshaller(method.inputType))
      .setResponseMarshaller(new Marshaller(method.outputType))
      .setSampledToLocalTracing(true)
      .build()
}
