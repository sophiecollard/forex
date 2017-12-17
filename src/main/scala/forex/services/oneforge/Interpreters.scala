package forex.services.oneforge

import java.time.OffsetDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import forex.config.OneForgeConfig
import forex.domain._
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.addon.monix.task._

object Interpreters {
  def dummy[R](
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Dummy[R]

  def live[R](
      oneForgeConfig: OneForgeConfig
  )(
      implicit
      m1: _task[R],
      sys: ActorSystem,
      mat: ActorMaterializer
  ): Algebra[Eff[R, ?]] = new Live[R](oneForgeConfig)
}

final class Dummy[R] private[oneforge] (
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] =
    for {
      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield Right(result)
}

final class Live[R] private[oneforge] (
    oneForgeConfig: OneForgeConfig
)(
    implicit
    m1: _task[R],
    sys: ActorSystem,
    mat: Materializer
) extends Algebra[Eff[R, ?]]
  with ErrorAccumulatingCirceSupport {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] = {
    val uri = s"${oneForgeConfig.baseUri}/quotes?pairs=${pair.from}${pair.to}&api_key=${oneForgeConfig.apiKey}"
    val request = HttpRequest(uri = uri)

    implicit val ec = sys.dispatcher

    for {
      response <- fromTask(Task.fromFuture(Http().singleRequest(request)))
      quotes <- fromTask(Task.fromFuture(Unmarshal(response.entity).to[List[Quote]]))
    } yield quotes match {
      case Nil    => Left(Error.ThirdParty(party = "1Forge", msg = "No quote returned"))
      case q :: _ => Right(Rate(pair, q.price, Timestamp.now))
    }
  }
}
