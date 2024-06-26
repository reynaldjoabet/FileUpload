import cats.effect._
import cats.effect.std.Random
import cats.effect.ExitCode

import com.comcast.ip4s._
import io.circe
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.defaults.Banner
import org.http4s.server.middleware.RequestLogger
import org.http4s.server.Server
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.LoggerName
import routes._
import services._

object Main extends IOApp {

//implicit val loggerName=LoggerName("name")
  implicit private val logger = Slf4jLogger.getLogger[IO]

  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(
      s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}"
    )

  override def run(args: List[String]): IO[ExitCode] =
    Random
      .scalaUtilRandom[IO]
      .flatMap { random =>
        EmberServerBuilder
          .default[IO]
          .withHttpApp(
            RequestLogger.httpApp(true, false)(
              FileRoutes(
                UploadService.make[IO],
                DownloadService.make[IO],
                random
              ).uploadRoutes.orNotFound
            )
          )
          .withPort(port"8085")
          .withHost(host"127.0.0.1")
          .build
          .evalTap(showEmberBanner[IO](_))
          .useForever
      }
      .as(ExitCode.Success)

}
