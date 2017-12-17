package forex.main

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.Eval
import forex.config._
import forex.{ services ⇒ s }
import forex.{ processes ⇒ p }
import org.zalando.grafter._
import org.zalando.grafter.macros._

import scala.concurrent.Await
import scala.concurrent.duration._

@readerOf[ApplicationConfig]
case class Processes(
    oneForgeConfig: OneForgeConfig
) extends Stop {

  implicit lazy val system = ActorSystem("live-interpreter")
  implicit lazy val materializer = ActorMaterializer()

  implicit final lazy val _oneForge: s.OneForge[AppEffect] = oneForgeConfig.interpreter match {
    case "dummy" => s.OneForge.dummy[AppStack]
    case "live"  => s.OneForge.live[AppStack](oneForgeConfig)
    case _       => throw new RuntimeException("Invalid oneforge interpreter")
  }

  final val Rates = p.Rates[AppEffect]

  override def stop: Eval[StopResult] =
    StopResult.eval("Processes") {
      Await.result(system.terminate(), 10.seconds)
    }

}
