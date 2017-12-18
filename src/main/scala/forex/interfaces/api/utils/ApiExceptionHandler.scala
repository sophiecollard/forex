package forex.interfaces.api.utils

import akka.http.scaladsl._
import akka.http.scaladsl.model.StatusCodes
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import forex.processes._
import forex.processes.rates.messages.Error.ThirdParty
import io.circe.syntax._

object ApiExceptionHandler extends FailFastCirceSupport {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case ThirdParty(party, reason) ⇒
        ctx ⇒
          val entity = Map(
            "error" -> "Third party error".asJson,
            "details" -> Map("party" -> party, "reason" -> reason).asJson
          ).asJson
          ctx.complete(StatusCodes.InternalServerError, entity)
      case _: RatesError ⇒
        ctx ⇒
          ctx.complete(StatusCodes.InternalServerError, "Something went wrong in the rates process")
      case _: Throwable ⇒
        ctx ⇒
          ctx.complete(StatusCodes.InternalServerError, "Something else went wrong")
    }

}
