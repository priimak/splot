package xyz.devfortress.splot.examples

import java.awt.Color
import java.util.Random

import xyz.devfortress.splot._

object ScatterPlotExample {
  def main(args: Array[String]): Unit = {
    val fig = Figure(
      xTicks = Ticks(-6 to 6), yTicks = Ticks(-5 to 5),
      domain = (-6, 6), range = (-5, 5),
      title = "Overlapping Gaussian Distributions",
      showGrid = true,
      xLabel = "x-axis",
      yLabel = "y-axis"
    )

    val rnd = new Random()

    val gs = (0 to 100000).map(_ => (rnd.nextGaussian() - 1.5, rnd.nextGaussian())) ++
      (0 to 100000).map(_ => (rnd.nextGaussian() + 1.5, rnd.nextGaussian()))

    fig.scatter(gs, pt = "o", color = new Color(0, 0, 0, 10))

    fig.show(730, 500)
  }
}
