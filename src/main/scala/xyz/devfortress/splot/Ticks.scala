package xyz.devfortress.splot

class Ticks(ticks: Seq[(Double, String)]) extends (((Double, Double)) => Seq[(Double, String)]) {
  override def apply(range: (Double, Double)): Seq[(Double, String)] =
    ticks.filter(_._1 >= range._1).filter(_._1 <= range._2)
}

object Ticks {
  def round(d: Double, decimalPlace: Int) =
    BigDecimal(d).setScale(decimalPlace, BigDecimal.RoundingMode.FLOOR).doubleValue()

  /**
   * Function that is passed as xTicks and yTicks parameter to [[Figure]] constructor which will make 10 or so ticks.
   */
  val ticks10: ((Double, Double)) => Seq[(Double, String)] =
    (range: (Double, Double)) => {
      assert(range._2 > range._1)
      val (decimalPoints, tickDistance) = Stream.from(1)
        .map(d => (d, round((range._2 - range._1)/10, d)))
        .filter(_._2 != 0)
        .head
      val firstTickPosition = round(range._1, decimalPoints)
      //noinspection ScalaMalformedFormatString
      (0 to 20).map(firstTickPosition + tickDistance * _)
        .filter(_ <= range._2)
        .filter(_ >= range._1)
        .map(tickPosition => (tickPosition, s"%.${decimalPoints}f".format(tickPosition)))
    }

  val none: ((Double, Double)) => Seq[(Double, String)] = _ => Seq()

  def apply(ticks: Double*): Ticks = new Ticks(ticks.map(t => (t, s"$t")))

  def apply(ticks: Range): Ticks = new Ticks(ticks.map(t => (t.toDouble, s"$t")))
}
