package xyz.devfortress.splot.tests

import java.awt._
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference

import org.scalatest.FunSuite
import xyz.devfortress.splot._

class FigureTest extends FunSuite {
  test("Simple Figure") {
    val g2capture = new AtomicReference[G2Proxy](null)
    val g2c: BufferedImage => Graphics2D  = image => {
      val g2 = new G2Proxy(image.createGraphics())
      g2capture.set(g2)
      g2
    }

    val fig = Figure(title = "A simple plot",
      domain = (0, 10), range = (0, 5),
      xTicks = Ticks.none, yTicks = Ticks.none,
      g2creator = g2c
    )
    fig.plot(Seq((0.0, 0.0), (1.0, 1.0), (2.0, 1.5)))
    fig.makeImage(800, 600)

    val graphics2D = g2capture.get()
    assert(graphics2D != null)
    for (x <- graphics2D.getCapture()) {
      println(x)
    }
    graphics2D.verify() { capturedCalls => {
      import capturedCalls._
      // setting up background for newly created image
      setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      setBackground(Color.WHITE)
      clearRect(0, 0, 800, 600)

      // draw two lines of black color and lw=1 corresponding to our plot
      setColor(Color.BLACK)
      setStroke(new BasicStroke(1))
      drawLine(50, 550, 120, 450)
      drawLine(120, 450, 190, 400)

      // draw bounding box
      getBackground
      getColor
      getStroke
      setBackground(Color.WHITE)
      // clear area around bounding box
      clearRect(0, 0, 800, 50)
      clearRect(0, 0, 50, 600)
      clearRect(0, 550, 800, 50)
      clearRect(750, 0, 50, 600)
      // reset saved background color
      setBackground(Color.WHITE)
      // prepare to draw box
      setColor(Color.BLACK)
      setStroke(new BasicStroke(2))
      drawRect(50, 50, 700, 500)
      // restore saved stroke and color
      setStroke(new BasicStroke(1))
      setColor(Color.BLACK)

      // draw title
      getFont
      getColor
      setFont(Font.decode("Times-20"))
      setColor(Color.BLACK)
      getFontMetrics()
      drawString("A simple plot", 344, 30)
      // restore saved font and color
      setColor(Color.BLACK)
      setFont(Font.decode("Dialog-12"))
    }}
  }
}
