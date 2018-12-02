package xyz.devfortress.splot

import java.awt.Color

package object colormaps {
  /**
   * Take color map data in a format similar to matplotlib data and generate colormap function.
   */
  def makeColormap(cData: Map[String, Seq[(Double, Double, Double)]]): Double => Color = {
    val r = cData("red")
    val redRanges = (r zip r.tail)
      .map(rr => ((rr._1._1, rr._2._1), rr._2._1 - rr._1._1, rr._1._3, rr._2._2 - rr._1._3))

    val g = cData("green")
    val greenRanges = (g zip g.tail)
      .map(rr => ((rr._1._1, rr._2._1), rr._2._1 - rr._1._1, rr._1._3, rr._2._2 - rr._1._3))

    val b = cData("blue")
    val blueRanges = (b zip b.tail)
      .map(rr => ((rr._1._1, rr._2._1), rr._2._1 - rr._1._1, rr._1._3, rr._2._2 - rr._1._3))

    value => {
      val nVal = if (value > 1) 1 else if (value < 0) 0 else value
      val redRange = redRanges.find(r => nVal >= r._1._1 && nVal <= r._1._2).get
      val greenRange = greenRanges.find(r => nVal >= r._1._1 && nVal <= r._1._2).get
      val blueRange = blueRanges.find(r => nVal >= r._1._1 && nVal <= r._1._2).get

      new Color(
        (((nVal - redRange._1._1) / redRange._2 * redRange._4 + redRange._3) * 255).toInt,
        (((nVal - greenRange._1._1) / greenRange._2 * greenRange._4 + greenRange._3) * 255).toInt,
        (((nVal - blueRange._1._1) / blueRange._2 * blueRange._4 + blueRange._3) * 255).toInt
      )
    }
  }

  val viridis: Double => Color = makeColormap(ColorMapsData.viridisData)
  val inferno: Double => Color = makeColormap(ColorMapsData.infernoData)
}
