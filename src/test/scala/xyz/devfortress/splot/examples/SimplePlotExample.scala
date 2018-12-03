package xyz.devfortress.splot.examples

import java.awt.Font
import java.lang.Math.abs

import xyz.devfortress.splot._

object SimplePlotExample {
  // Function that we will plot
  def f(x: Double): Double = Math.sin(x) / x

  // Derivative of the above function
  def df(x: Double): Double = Math.cos(x) / x - Math.sin(x) / (x * x)

  def main(args: Array[String]): Unit = {
    val fig = new Figure(
      xTicks = Ticks(0 to 50 by 2),
      domain = (0, 20),
      antialiasing = true,
      title = "Simple Plot Example"
    )

    // Plot heat map like plot of (x,y)=>z function that.
    fig.map(f = (x, _) => x, xDomain = (7, 15), yDomain = (-0.4, -0.2))//, zRange = (-2.5, -0.5))

    // Plot function f(x) as line plot.
    val fdata: Seq[(Double, Double)] = (0.1 to 20.0 by 0.2).map(x => (x, f(x)))
    fig.plot(fdata, lw = 4, color = "orange")

    // Plot number of cross-points along the line plot.
    fig.scatter((0.2 to 20.0 by 0.6).map(x => (x, f(x))), ps = 20, pt = "+", color = "black")

    // Compute derivatives at following points.
    val x = Seq(3.7, 4.6, 7)

    // Plot derivatives as tangent lines.
    x.foreach(x => {
      fig.plot(Seq((x - 3, f(x) - 3 * df(x)), (x + 3, f(x) + 3 * df(x))))
    })

    // Draw filled circles at points where we take derivative. Color of the points will depend on value of
    // derivative at the points.
    import java.awt.Color
    val maxDerivative = x.map(f).map(abs).max
    val zValues: Seq[Double] = x.map(df).map(_ / maxDerivative).map(abs)
    fig.zscatter(x.map(x => (x, f(x))), zValues, pt = "o", ps = 20, colorMap = colormaps.inferno)

    // Plot three rectangles. First rectangle has only boundary drawn.
    fig.rectangle((15, 0.3), 1, 0.3)

    // Second rectangle has boundaries drawn as lines and also filled with blue (transparency used by default is 0.2).
    fig.rectangle((13.7, 0.34), 3, 0.1, fillColor = "blue")

    // Third rectangle will not have boundaries drawn, only filled with color red and custom transparency of 0.6.
    fig.rectangle((13.3, 0.38), 2.6, 0.2, fillColor = "red", lw = 0, alpha = 0.6)

    // Draw arbitrary polygon shape in outline of black lines of lineWidth 5 and filled in nontransparent pink.
    fig += Shape(
      Seq((8.2, 0.6), (12, 0.5), (9.4, 0.95), (8.8, 0.93)),
      fillColor = Some(Color.PINK), color = Color.BLACK, lineWidth = 5
    )

    fig += Label("sin(x)/x", 2, 0.5, Font.decode("Arial-18"))

    // Display window with the plot.
    fig.show()
  }
}
