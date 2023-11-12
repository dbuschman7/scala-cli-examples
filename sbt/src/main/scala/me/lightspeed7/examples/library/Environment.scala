package me.lightspeed7.examples.library

import scala.collection.MapView
import scala.collection.concurrent.TrieMap

class Environment extends Logger {

  def clearValue(key: String): Option[String] = {
    stdOut(s"WARNING WARNING -- Danger Will Robinson - Env Var '$key' has been cleared")
    Environment.valuesMap.remove(key)
  }

  def overrideValue(key: String, value: String): Unit = {
    Environment.valuesMap.put(key, value)
    stdOut(s"WARNING WARNING -- Danger Will Robinson - Env Var '$key' is now set to '$value'")
  }

  def value(name: String): Option[String] = Option(name).flatMap(f => Environment.valuesMap.get(f))

  def value(name: String, `default`: String): String = value(name).getOrElse(`default`)

  def valueTo[T](name: String)(block: String => Option[T]): Option[T] = value(name).map(_.trim).flatMap(block)

}

object Environment {
  def apply() = new Environment()

  def map: MapView[String, String] = valuesMap.view

  lazy val valuesMap: TrieMap[String, String] = {
    val m = new TrieMap[String, String]()
    // load sys props first
    sys.props.map { case (k, v) => m.put(k.toUpperCase.replaceAll("\\.", "_"), v) }
    // load env vars second as overrides
    sys.env.map { case (k, v) => m.put(k, v) }
    //
    m
  }
}

