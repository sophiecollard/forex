package forex.services.oneforge

import forex.config.ApplicationConfig
import forex.domain._
import forex.main.Processes
import forex.utils.AwaitHelper
import monix.execution.Scheduler.Implicits.global
import org.atnos.eff.syntax.addon.monix.task._
import org.specs2.mutable.Specification

class InterpretersSpec
  extends Specification
    with AwaitHelper {

  val appConfig = pureconfig.loadConfig[ApplicationConfig]("app").toOption.get // not safe!

  val dummyProcesses = Processes(appConfig.oneforge.copy(interpreter = "dummy"))
  val dummyInterpreter = dummyProcesses._oneForge

  val liveProcesses = Processes(appConfig.oneforge.copy(interpreter = "live"))
  val liveInterpreter = liveProcesses._oneForge

  "Dummy interpreter" should {
    "get rate" in {
      val pair = Rate.Pair(Currency.GBP, Currency.JPY)
      val maybeRate = await {
        dummyInterpreter
          .get(pair)
          .runAsync
          .runAsync
      }
      maybeRate must beRight[Rate]
    }
  }

  "Live interpreter" should {
    "get rate" in {
      val pair = Rate.Pair(Currency.GBP, Currency.JPY)
      val maybeRate = await {
        liveInterpreter
          .get(pair)
          .runAsync
          .runAsync
      }
      maybeRate must beRight[Rate]
    }
  }

}
