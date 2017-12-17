package forex.services.oneforge

import forex.domain.Price
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

// 1Forge quotes will contain other attributes but we can ignore those
final case class Quote(price: Price)

object Quote {
  implicit val encoder: Encoder[Quote] = deriveEncoder
  implicit val decoder: Decoder[Quote] = deriveDecoder
}
