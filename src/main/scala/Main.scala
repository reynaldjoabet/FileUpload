import cats.effect.ExitCode
import cats.effect._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import org.http4s.server.defaults.Banner
import org.http4s.server.Server
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.LoggerName
import io.circe
import routes._
import services._
import org.http4s.server.middleware.RequestLogger
object Main extends IOApp {
//implicit val loggerName=LoggerName("name")
  private implicit val logger = Slf4jLogger.getLogger[IO]

  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(
      s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}"
    )

  override def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHttpApp(
        RequestLogger.httpApp(true, false)(
          UploadRoutes(UploadService.make[IO]).uploadRoutes.orNotFound
        )
      )
      .withPort(port"8083")
      .withHost(host"127.0.0.1")
      // .withLogger(logger)
      // .withTLS()
      // .withHostOption()
      .build
      .evalTap(showEmberBanner[IO](_))
      .useForever
      .as(ExitCode.Success)

}
