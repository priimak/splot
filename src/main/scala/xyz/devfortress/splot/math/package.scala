package xyz.devfortress.splot

import java.util

import scala.jdk.CollectionConverters._

package object math {
  /**
   * Creates sequence of `Doubles` starting from `start` and ending at or before `end` with step `step`. This function
   * is needed since use of ranges for doubles was removed from Scala 2.13.x
   */
  def mkSeq[A: Integral](start: A, end: A, step: A): Seq[Double] = {
    val integral = implicitly[Integral[A]]
    var lastElement = integral.toDouble(start)
    val endElement = integral.toDouble(end)
    val dt = integral.toDouble(step)
    val res = new util.ArrayList[Double](((endElement - lastElement) / dt).toInt + 3)
    res.add(lastElement)

    while (lastElement < endElement) {
      lastElement = lastElement + dt
      if (lastElement <= endElement) {
        res.add(lastElement)
      }
    }
    res.iterator.asScala.toSeq
  }
}
