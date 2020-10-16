package restui.providers.git.vcs

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Merge, Source => AkkaSource}
import restui.models.ServiceEvent
import restui.protobuf.ProtobufCompiler
import restui.providers.git._
import restui.providers.git.git.Git
import restui.providers.git.github.{Github, GithubClient}
import restui.providers.git.settings.{GitSettings, GithubSettings, Settings}

import scala.concurrent.ExecutionContext

object VCS {
  def source(settings: Settings, requestExecutor: RequestExecutor)(implicit
      actorSystem: ActorSystem[_],
      executionContext: ExecutionContext,
      protobufCompiler: ProtobufCompiler): Source[ServiceEvent] =
    settings.vcs.map {
      case githubSettings: GithubSettings =>
        Git.fromSource(settings.cacheDuration,
                       Github(GithubClient(githubSettings, requestExecutor)))
      case GitSettings(repositories) =>
        Git.fromSettings(settings.cacheDuration, repositories)
    }.fold(AkkaSource.empty[ServiceEvent]) { (acc, source) =>
      AkkaSource.combine(acc, source)(Merge(_))
    }
}
