package forex.processes.rates

import java.time.OffsetDateTime

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.implicits._
import forex.domain._
import forex.services._

import scala.collection.mutable.Map
import scala.concurrent.duration._

object Processes {
  def apply[F[_]]: Processes[F] =
    new Processes[F] {}
}

trait Processes[F[_]] {
  import messages._
  import converters._

  protected val cache: Map[Rate.Pair, Rate] = Map.empty

  def get(
      request: GetRequest
  )(
      implicit
      M: Monad[F],
      OneForge: OneForge[F]
  ): F[Error Either Rate] = {
    val pair = Rate.Pair(request.from, request.to)

    val cachedResult = OptionT(cache.get(pair).pure[F])
      .filter { rate =>
        val secondsSinceRetrieved = OffsetDateTime.now.toEpochSecond - rate.timestamp.value.toEpochSecond
        secondsSinceRetrieved < 5.minutes.toSeconds
      }
      .toRight[Error](Error.Generic)

    val result = cachedResult
      .orElse {
        EitherT(OneForge.get(pair)).map { rate =>
          cache += (pair -> rate)
          rate
        }
      }
      .leftMap(toProcessError)

    result.value
  }

}
