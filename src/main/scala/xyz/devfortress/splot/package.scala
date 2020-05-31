package xyz.devfortress

import java.awt.Graphics2D

import scala.util.Try

package object splot {
  implicit def range2OptionD(range: (Double, Double)): Option[(Double, Double)] = Option.apply(range)
  implicit def range2OptionI(range: (Int, Int)): Option[(Double, Double)] = Some((range._1.toDouble, range._2.toDouble))

  implicit def ran2op[T](r: (T, T))(implicit integral: Integral[T]): Option[(Double, Double)] =
    Some((integral.toDouble(r._1), integral.toDouble(r._2)))

  implicit val range2OptionDV: ((Double, Double)) => Option[(Double, Double)] = range2OptionD
  implicit val range2OptionIV: ((Int, Int)) => Option[(Double, Double)] = range2OptionI

  implicit def gr2helper(g2: Graphics2D): Graphics2DHelper = Graphics2DHelper(g2)

  val derivedDomain: (Double, Double) => Boolean = (_, _) => true

  /**
   * Reintroduced from Scala 2.12.x
   */
  implicit object DoubleAsIfIntegral extends Integral[Double] {
    override def quot(x: Double, y: Double): Double = (BigDecimal(x) quot BigDecimal(y)).doubleValue
    override def rem(x: Double, y: Double): Double = (BigDecimal(x) remainder BigDecimal(y)).doubleValue
    override def plus(x: Double, y: Double): Double = x + y
    override def minus(x: Double, y: Double): Double = x - y
    override def times(x: Double, y: Double): Double = x * y
    override def negate(x: Double): Double = -x
    override def fromInt(x: Int): Double = x.toDouble
    override def parseString(str: String): Option[Double] = Try(str.toDouble).toOption
    override def toInt(x: Double): Int = x.toInt
    override def toLong(x: Double): Long = x.toLong
    override def toFloat(x: Double): Float = x.toFloat
    override def toDouble(x: Double): Double = x
    override def abs(x: Double): Double = Math.abs(x)
    override def compare(x: Double, y: Double): Int = x.compareTo(y)
  }

  // Type-class STextLike
  trait STextLike[-A] {
    def asSText(a: A): SText
  }

  object STextLike {
    implicit object StringIsSTextLike extends STextLike[String] {
      override def asSText(text: String): SText = {
        if (text.startsWith("$") && text.endsWith("$"))
          LaTeXText(text.substring(1, text.length - 1)) // assume that it is a latex
        else
          PlainText(text)
      }
    }

    implicit object STextIsSText extends STextLike[SText] {
      override def asSText(sText: SText): SText = sText
    }

    def apply[A](implicit sTextLike: STextLike[A]): STextLike[A] = sTextLike
  }

  // any to Option[A]
  implicit def anyToOption[A](a: A): Option[A] = Option(a)
}
