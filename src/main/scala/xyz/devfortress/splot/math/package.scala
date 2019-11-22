package xyz.devfortress.splot

import java.util

import scala.collection.JavaConverters

package object math {
  /**
   * Creates sequence of `Doubles` starting from `start` and ending at or before `end` with step `step`. This function
   * is needed since use of ranges for doubles was removed from Scala 2.13.x
   */
  def mkSeq[A: Integral](start: A, end: A, step: A)(implicit integral: Integral[A]): Seq[Double] = {
    val res = new util.ArrayList[Double]()
    var lastElement = integral.toDouble(start)
    val endElement = integral.toDouble(end)
    val dt = integral.toDouble(step)
    res.add(lastElement)

    while (lastElement < endElement) {
      lastElement = lastElement + dt
      if (lastElement <= endElement) {
        res.add(lastElement)
      }
    }
    JavaConverters.asScalaIteratorConverter(res.iterator).asScala.toSeq
  }
}
