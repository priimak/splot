package xyz.devfortress.splot.examples

import java.awt.Color
import java.lang.Math.{pow, sin}

import xyz.devfortress.splot._
import xyz.devfortress.splot.math._

object FunctionPlotExample {
  def f(x: Double): Double = sin(pow(x, 3)) / x

  def main(args: Array[String]): Unit = {
    val fig = Figure(
      title = """$y = sin(x^2)/x$""",
      titleFontSize = 30,
      showGrid = true,
      xTicks = Ticks(1 to 4),
      xLabel = "x",
      yLabel = "y"
    )

    val xs = mkSeq(1, 5, 0.001)
    fig.plot(xs.map(x => (x, 1/x)), color = "blue", lw = 2, lt = "--")
    fig.plot(xs.map(x => (x, f(x))))
    fig.add(Label("""$\frac{\sin(x^2)}{x}$""", 1.37, f(1.36), fontSize = 27, anchor = Anchor.LEFT_UPPER))
    fig.add(Label("1/x", x = 1.54, y = 1/1.5, color = Color.RED, angle = -30, fontSize = 20))
    fig.show(730, 500)
  }
}
