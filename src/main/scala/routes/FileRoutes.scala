package routes

import cats.effect.kernel.Async
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.all._
import cats.Parallel

import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.Multipart
import org.http4s.multipart.Multiparts
import org.http4s.multipart.Part
import org.http4s.Status._
import services._

final case class FileRoutes[F[_]: Async: Parallel: Console](
  uploadService: UploadService[F],
  downloadService: DownloadService[F],
  random: Random[F]
) extends Http4sDsl[F] {

  val uploadRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case r @ POST -> Root / "upload" =>
      implicitly[EntityDecoder[F, Multipart[F]]]
        .decode(r, strict = false)
        .value
        .flatMap {
          case Left(_) => BadRequest()
          case Right(mp) =>
            mp.parts
              .parTraverse_ { part =>
                val partFileName = part.filename.get
                uploadService.upload(partFileName, part.body).&>(Console[F].println(part))
              }
              .flatMap(Ok(_))

        }

    case req @ GET -> Root / "download" / fileName =>
      val entityBody = downloadService.download("unnamed.png")
      val part       = Part.fileData("unnamed", "unnamed.png", entityBody)
      Multiparts.fromRandom(random).multipart(Vector(part)).flatMap(parts => Ok(parts))

    case req @ POST -> Root / "upload" / IntVar(sizeLimit) =>
      req
        .toStrict(Some(sizeLimit))
        .flatMap(_.as[Multipart[F]])
        .flatMap { mp =>
          mp.parts
            .parTraverse_ { part =>
              val partFileName = part.filename.get
              uploadService.upload(partFileName, part.body).&>(Console[F].println(part))
            }
        }
        .flatMap(Ok(_))
  }

}
