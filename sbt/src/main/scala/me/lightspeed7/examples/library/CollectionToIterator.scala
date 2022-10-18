package me.lightspeed7.examples.library

object CollectionToIterator {

  def toEndlessStream[T](input: Seq[T]): Iterator[T] = {

//    println(s"Size = $input.size")
    new Iterator[T] {
      var idx: Int = 0

      override def hasNext: Boolean = true

      override def next(): T = {
        val modulus: Int = idx % input.size
//        println(s"next() - $idx - $modulus - ${input.size} ")
        idx = idx + 1
        input(modulus)
      }
    }

  }
}
