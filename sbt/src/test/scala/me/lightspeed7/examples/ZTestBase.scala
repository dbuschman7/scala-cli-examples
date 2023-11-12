package me.lightspeed7.examples

import zio._

trait ZTestBase {

  private val runtime = Runtime.default

  def runAsyncTest(program: ZIO[Any, Any, ExitCode]): ExitCode = {
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(program).getOrThrowFiberFailure()
    }
  }

}
