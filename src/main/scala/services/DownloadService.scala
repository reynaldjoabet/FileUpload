package services
import fs2.Stream
import cats.effect.kernel.Async
import fs2.io.file._

trait DownloadService[F[_]] {
  def download(fileName: String): Stream[F, Byte]
}

object DownloadService {
  def make[F[_]: Async] = new DownloadService[F] {
    override def download(fileName: String): Stream[F, Byte] = {
      val fileStoreLocation = s"./src/main/upload/$fileName"
      val target = Path(fileStoreLocation)
      Files[F].readAll(target)
    }

  }
}
