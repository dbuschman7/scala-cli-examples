package me.lightspeed7.examples.library

object CollectionToStream {


  def toEndlessStream[T](in: Seq[T]): Iterator[T] = {
    val size = in.size

    new Iterator[T] {
      var idx: Int = 0

      override def hasNext: Boolean = true

      override def next(): T = {
        println("next()")
        val pos = idx % size
        val value = in(pos)
        idx += 1
        value
      }
    }
    
  }
}
