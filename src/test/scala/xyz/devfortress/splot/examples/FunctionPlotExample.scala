package xyz.devfortress.splot.examples

import java.awt.geom.AffineTransform
import java.awt.{Color, Font}
import java.lang.Math.{pow, sin}

import xyz.devfortress.splot._
import xyz.devfortress.splot.math._

object FunctionPlotExample {
  def f(x: Double): Double = sin(pow(x, 3)) / x

  def main(args: Array[String]): Unit = {
    val fig = Figure(
      title = "sin(x^2)/x",
      showGrid = true,
      xTicks = Ticks(1 to 4),
    )

    val xs = mkSeq(1, 5, 0.001)
    fig.plot(xs.map(x => (x, 1/x)), color = "blue", lw = 2, lt = "--")
    fig.plot(xs.map(x => (x, f(x))))
    fig.add(Label("sin(x^2)/x", 1.37, f(1.36), font = Font.decode("Times-18")))
    fig.add(Label(
      "1/x", 1.54, 1/1.5,
      font = Font.decode("Serif-25").deriveFont(AffineTransform.getRotateInstance(0.5)),
      color = Color.RED
    ))
    fig.show(730, 500)
  }
}
