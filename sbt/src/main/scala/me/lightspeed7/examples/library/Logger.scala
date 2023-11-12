package me.lightspeed7.sk8s.aiq.k8s

trait Logger {

  def stdOut(msg: String): Unit = Console.out.println(msg)

  def stdErr(msg: String): Unit = Console.err.println(msg)
}
