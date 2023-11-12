package me.lightspeed7.examples.library

trait Logger {

  def stdOut(msg: String): Unit = Console.out.println(msg)

  def stdErr(msg: String): Unit = Console.err.println(msg)
}
