package xyz.devfortress.splot.examples

import java.awt.Color
import java.lang.Math.{pow, sin}

import xyz.devfortress.splot.{Figure, Ticks, _}

object HeatmapPlotExample {

  def main(args: Array[String]): Unit = {
    val fig = Figure(yTicks = Ticks(), xTicks = Ticks(),
      topPadding = 1, bottomPadding = 1, leftPadding = 1, rightPadding = 1,
      bgcolor = Color.BLACK)

    def w(x: Double, y: Double, phase: Double): Double = {
      val r = pow(pow(x,2) + pow(y, 2), 0.5)
      if (r < 0.5) 0 else sin(3 * (r + phase))/pow(r, 0.3)
    }

    fig.map((x, y) => w(x, y, 0) * w (x - 2, y + 1, 1.3), xDomain = (-2, 7), yDomain = (-2, 3.5),
      inDomain = (x, y) => {
        val r1 = pow(pow(x,2) + pow(y, 2), 0.5)
        val r2 = pow(pow(x - 2,2) + pow(y + 1, 2), 0.5)
        r1 > 0.5 && r2 > 0.5
      })

    fig.show(730, 500)
  }
}
