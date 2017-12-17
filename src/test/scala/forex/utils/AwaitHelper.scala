package forex.utils

import scala.concurrent.{Await, Awaitable}
import scala.concurrent.duration._

trait AwaitHelper {

  def await[T](f: => Awaitable[T], timeout: FiniteDuration = 10.seconds): T = {
    Await.result(f, timeout)
  }

}
