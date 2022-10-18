package me.lightspeed7.examples.library

object PrettyPrint {

  private val units = Array[String]("B", "K", "M", "G", "T")
  private val format = new java.text.DecimalFormat("#,##0.#")

  def fileSizing(input: Long): String = {
    if (input <= 0) return "0.0"
    val digitGroups = (Math.log10(input.toDouble) / Math.log10(1024)).floor.toInt
    format.format(input / Math.pow(1024.0, digitGroups.toDouble)) + " " + units(digitGroups)
  }

  def number(input: Long): String = format.format(input)

  def latency(timeInNanos: Long): String = {
    val micro = timeInNanos / 1000
    val millis = micro / 1000
    val rawSecs: Int = (millis.toDouble / 1000).floor.toInt
    val mins: Int = rawSecs / 60
    val secs: Int = rawSecs - (mins * 60)

    (mins, secs, millis) match {
      case (m, s, _) if m > 0 => s"$m mins $s seconds"
      case (_, s, _) if s > 0 => s"$s seconds"
      case (_, _, m) if m > 0 => s"$millis milliseconds"
      case (_, _, _) => s"$micro microseconds"
    }
  }
}
