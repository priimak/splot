package xyz.devfortress.splot.examples

import xyz.devfortress.splot._

import scala.collection.immutable

object SimplePlotExample {
  // function that we will plot
  def f(x: Double): Double = Math.sin(x) / x

  // derivative of the above function
  def df(x: Double): Double = Math.cos(x) / x - Math.sin(x) / (x * x)

  def main(args: Array[String]): Unit = {
    val fig = Figure()

    // plot function f(x) as line plot
    val fdata: Seq[(Double, Double)] = (0.1 to 20.0 by 0.2).map(x => (x, f(x)))
    fig.plot(fdata, lw = 4, color = "orange")

    fig.scatter((0.2 to 20.0 by 0.6).map(x => (x, f(x))), ps = 20, pt = "+", color = "black")

    // compute derivatives at following points
    val x = Seq(4.9, 6.8, 8)

    // plot derivatives as tangent lines
    x.foreach(x => {
      fig.plot(Seq((x - 3, f(x) - 3 * df(x)), (x + 3, f(x) + 3 * df(x))))
    })

    // draw filled circles at points where we take derivative
    import java.awt.Color
    fig.scatter(x.map(x => (x, f(x))), pt = "o", ps = 20, color = Color.RED)

    // plot three rectangles
    // first rectangle has only boundary drawn
    fig.rectangle((15, 0.3), 1, 0.3)

    // second rectange has boundaries drawn as lines and also filled with blue (transparency used by default is 0.2)
    fig.rectangle((13.7, 0.34), 3, 0.1, fillColor = "blue")

    // third rectangle will not have boundaries drawn, only filled with color red and custom transparency of 0.6
    fig.rectangle((13.3, 0.38), 2.6, 0.2, fillColor = "red", lw = 0, alpha = 0.6)

    // draw arbitrary polygon shape in outline of black lines of lineWidth 5 and filled in nontransparent pink
    fig += Shape(
      Seq((8.2, 0.6), (12, 0.5), (9.4, 0.95), (8.8, 0.93)),
      fillColor = Some(Color.PINK), color = Color.BLACK, lineWidth = 5
    )

    // display window with the plot
    fig.show()
  }
}
