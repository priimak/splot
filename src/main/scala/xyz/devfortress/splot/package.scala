package xyz.devfortress

package object splot {
  implicit def range2OptionD(range: (Double, Double)): Option[(Double, Double)] = Option.apply(range)
  implicit def range2OptionI(range: (Int, Int)): Option[(Double, Double)] = Some((range._1.toDouble, range._2.toDouble))

  implicit val doubleAsIfIntegral = scala.math.Numeric.DoubleAsIfIntegral

  val derrivedDomain: (Double, Double) => Boolean = (_, _) => true
}
