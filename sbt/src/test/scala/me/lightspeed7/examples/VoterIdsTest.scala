package me.lightspeed7.examples

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

import scala.collection.MapView

class VoterIdsTest extends AnyFunSuite with Matchers {

  test("challenge 1") {
    // find the 3 generated ids
    val ids = Seq(10014, 10235, 10236, 10555, 10721, 11001, 11022, 12310, 12555, 13555)

    println("Total Ids -> " + ids.size)
    println("Ids       -> " + ids.mkString("[", ", ", "]"))
    println("Checksum  -> " + ids.filter(sumWithEndChkSum).mkString("[", ", ", "]"))
    println("DivByFive -> " + ids.filterNot(divByFive).mkString("[", ", ", "]"))

    calculateSpecialNumPercentage("Tiny Counties", 1000)
    calculateSpecialNumPercentage("Small Counties", 10000)
    calculateSpecialNumPercentage("Medium Counties", 100000)
    calculateSpecialNumPercentage("large Counties", 1000000)

    val (special, _) = (1 to 1000000).partition(sumWithEndChkSum)
    val bucketCounts: MapView[Int, Int] = special
      .map { id: Int =>
        val bucket: Int = id / 10000
        (bucket, id)
      }
      .groupBy(_._1)
      .view
      .mapValues(_.size)

    bucketCounts.toSeq.sortBy(_._1).map {
      case (b, c) => println(f"$b%5d : $c%d")
    }

  }

  test("Challenge 2") {
    val ids = Map(
      1234567 -> false,
      1003211 -> true,
      2371722 -> false,
      4509212 -> false,
      9873543 -> true,
      5732087 -> false,
      1953329 -> true,
      2294231 -> false,
      7247879 -> false,
      8076810 -> false,
      1009211 -> true, //
    )
    ids.map {
      case (id, correct) =>
        val digits = id.toString.map(charToInt)
        val last2  = digits.slice(digits.length - 2, digits.length)
        val last3  = digits.slice(digits.length - 3, digits.length)
        val last4  = digits.slice(digits.length - 4, digits.length)
        val last5  = digits.slice(digits.length - 5, digits.length)

        val init2          = digits.slice(0, digits.length - 2)
        val plusMinusInit  = plusMinus(digits.init)
        val plusMinusInit2 = plusMinus(init2)

        val sum2 = digitSum(digits.take(2))
        val sum3 = digitSum(digits.take(3))
        val sum4 = digitSum(digits.take(4))
        val sum5 = digitSum(digits.take(5))

        val plusMinus2 = plusMinus(digits.take(2))
        val plusMinus3 = plusMinus(digits.take(3))
        val plusMinus4 = plusMinus(digits.take(4))
        val plusMinus5 = plusMinus(digits.take(5))

        val letter: String = if (correct) "T" else " "
        val initSum        = digits.init.sum
        val last           = digits.last

        val test_l2 = digitSum(last2.sum)
        val test_l3 = digitSum(last3.sum)
        val test_l4 = digitSum(last4.sum)
        val test_l5 = digitSum(last5.sum)

        val try1 = digitSum(sum2 + test_l2)
//        val try2 = plusMinusInit2

        if (correct) {
          val firsts        = f"$sum2%3d $sum3%3d $sum4%3d $sum5%3d"
          val lasts       = f"$test_l2%3d $test_l3%3d $test_l4%3d $test_l5%3d"
          val plusMinuses = f"$plusMinus2%3d $plusMinus3%3d $plusMinus4%3d $plusMinus5%3d"

          val tests = f"$try1%3d"
          println(
            f"$id%10s ( ${init2.mkString("")} ${last2.head}%3d $last%3d ) :$letter:  $firsts    $lasts     $plusMinuses   :$letter: $tests "
          )
        }

    }

  }

  // Algorithms

  def plusMinus(in: Seq[Int]): Int =
    in
      .zipWithIndex
      .foldLeft(0) {
        case (prev, (digit, idx)) =>
          if (idx % 2 == 0) prev - digit else prev + digit
      }

  def digitSum(in: Int): Int = in.toString.map(charToInt).sum
  def digitSum(in: Seq[Int]): Int = digitSum(in.sum)

  def sumWithEndChkSum(in: Int): Boolean = {
    val digits = in.toString.map(charToInt)
    val sum: Int = {
      val temp = digits.init.sum
      if (temp < 10) { temp }
      else {
        temp.toString.map(charToInt).sum
      }
    }
    sum == digits.last
  }

  def divByFive(in: Int): Boolean = {
    val digits = in.toString.map(_.toInt - '0'.toInt)
    digits.drop(2).sum % 5 == 0
  }

  def charToInt(in: Char): Int = in.toInt - '0'.toInt

  def calculateSpecialNumPercentage(label: String, size: Int): Unit = {
    val (special, normal) = (1 to size).partition(sumWithEndChkSum)
    println(
      f"$label%20s(${commas(size)}%10s) -> special ${commas(special.size)}%10s  - normal ${commas(normal.size)}%10s  - ${sizing((special.size * 1000) / size)}"
    )

  }

  private val units  = Array[String]("B", "K", "M", "G", "T")
  private val format = new java.text.DecimalFormat("#,##0.#")

  def sizing(input: Long): String = {
    if (input <= 0) return "0.0"
    val digitGroups = (Math.log10(input.toDouble) / Math.log10(1024)).floor.toInt
    format.format(input / Math.pow(1024.0, digitGroups.toDouble)) + " " + units(digitGroups)
  }

  def commas(input: Long): String = format.format(input)

}
