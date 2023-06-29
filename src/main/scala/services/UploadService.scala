package services
import cats.Applicative
import fs2.io.file.Path
import fs2.io.file._
import fs2.Stream._
import cats.effect.kernel.Async
import cats.syntax.all._

trait UploadService[F[_]] {

  def upload(
      partFileName: String,
      body: fs2.Stream[F, Byte]
  ): F[Unit]

}

object UploadService {

  def make[F[_]: Async] =
    new UploadService[F] {
      def upload(
          partFileName: String,
          body: fs2.Stream[F, Byte]
      ): F[Unit] = {
        val fileStoreLocation = s"./src/main/upload/$partFileName"
        val target = Path(fileStoreLocation)
        body.through(Files[F].writeAll(target)).compile.drain
      }

    }

}
