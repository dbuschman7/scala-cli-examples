package me.lightspeed7.examples.library

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object Timer {

  def forgetMeNot(duration: FiniteDuration)(handler: Long => Unit)(implicit ex: ExecutionContext): ForgetMeNotTimer = {
    new ForgetMeNotTimer(duration)(handler)
  }
}

sealed class ForgetMeNotTimer(timeout: FiniteDuration)(handler: Long => Unit)(implicit ec: ExecutionContext) extends AutoCloseable {

  val lastUpdated: AtomicLong = new AtomicLong(System.currentTimeMillis())
  val running: AtomicBoolean = new AtomicBoolean(true)

  val intervalMillis: Long = timeout.toMillis
  val timeoutMillis: Long = timeout.toMillis

  val future: Future[Unit] = Future {
    while (running.get()) {
      Thread.sleep(intervalMillis)
      if (System.currentTimeMillis() - lastUpdated.get() > timeoutMillis) {
        handler(lastUpdated.get())
      }
    }
  }

  final def pingLastUpdate(): Unit = lastUpdated.set(System.currentTimeMillis())

  final def setLastUpdate(millis: Long = System.currentTimeMillis()): Unit = lastUpdated.set(millis)

  override def close(): Unit = running.set(false)
}

class TimerTest extends AnyFlatSpec with Matchers {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  "timers" should "forgetMeNot should not trigger if updates" in {
     val timer = Timer.forgetMeNot(2.seconds){ millis:Long => fail("Should not run")}
    1 to 5 foreach { _ =>
      Thread.sleep(500)
      timer.pingLastUpdate()
    }
    timer.close()
    Thread.sleep(2500) // make sure it doe
  }

  "timers" should "forgetMeNot should trigger without update" in {
    var called = false
    val timer = Timer.forgetMeNot(2.seconds) { millis: Long => called = true }
    Thread.sleep(2500) // make sure it doe
    timer.close()
    called mustBe true
  }
}