package xyz.devfortress.splot

class Ticks(ticks: Seq[(Double, String)]) extends ((Double, Double) => Seq[(Double, String)]) {

  override def apply(min: Double, max: Double): Seq[(Double, String)] =
    ticks.filter(_._1 >= min).filter(_._1 <= max)
}

object Ticks {
  def round(d: Double, decimalPlace: Int) =
    BigDecimal(d).setScale(decimalPlace, BigDecimal.RoundingMode.FLOOR).doubleValue()

  /**
   * Function that is passed as xTicks and yTicks parameter to [[Figure]] constructor which will make 10 or so ticks.
   */
  val ticks10: (Double, Double) => Seq[(Double, String)] =
    (min: Double, max: Double) => {
      assert(max > min)
      val (decimalPoints, tickDistance) = Stream.from(1)
        .map(d => (d, round((max - min)/10, d)))
        .filter(_._2 != 0)
        .head
      val firstTickPosition = round(min, decimalPoints)
      //noinspection ScalaMalformedFormatString
      (0 to 20).map(firstTickPosition + tickDistance * _)
        .filter(_ <= max)
        .filter(_ >= min)
        .map(tickPosition => (tickPosition, s"%.${decimalPoints}f".format(tickPosition)))
    }

  val none: (Double, Double) => Seq[(Double, String)] = (_, _) => Seq()

  def apply(ticks: Double*): Ticks = new Ticks(ticks.map(t => (t, s"$t")))

  def apply(ticks: Range): Ticks = new Ticks(ticks.map(t => (t.toDouble, s"$t")))
}
