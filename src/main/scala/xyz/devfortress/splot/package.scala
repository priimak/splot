package xyz.devfortress

package object splot {
  implicit def range2OptionD(range: (Double, Double)): Option[(Double, Double)] = Option.apply(range)
  implicit def range2OptionI(range: (Int, Int)): Option[(Double, Double)] = Some((range._1.toDouble, range._2.toDouble))

  implicit def ran2op[T](r: (T, T))(implicit integral: Integral[T]): Option[(Double, Double)] =
    Some((integral.toDouble(r._1), integral.toDouble(r._2)))

  implicit val doubleAsIfIntegral = scala.math.Numeric.DoubleAsIfIntegral

  implicit val range2OptionDV: ((Double, Double)) => Option[(Double, Double)] = range2OptionD
  implicit val range2OptionIV: ((Int, Int)) => Option[(Double, Double)] = range2OptionI

  val derivedDomain: (Double, Double) => Boolean = (_, _) => true
}
