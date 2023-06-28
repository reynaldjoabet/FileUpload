package routes
import cats.effect.kernel.Async
import org.http4s._
import org.http4s.Status._
import cats.syntax.all._
import org.http4s.dsl.Http4sDsl
import services._
import cats.Parallel
import cats.effect.std.Console
final case class UploadRoutes[F[_]: Async:Parallel:Console](uploadService: UploadService[F]) extends Http4sDsl[F] {

  val uploadRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case r @ POST -> Root / "upload" =>
      implicitly[EntityDecoder[F, multipart.Multipart[F]]]
        .decode(r, strict = false)
        .value
        .flatMap{
          case Left(_) => BadRequest()
          case Right(mp) =>
            mp.parts.parTraverse_ { part =>
              val partFileName = part.filename.get
              uploadService.upload(partFileName, part.body).&>(Console[F].println(part))
            }.flatMap(Ok(_))
        }
    case _ => BadRequest()
  }

}
