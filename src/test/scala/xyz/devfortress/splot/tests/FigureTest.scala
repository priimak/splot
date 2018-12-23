package xyz.devfortress.splot.tests

import java.awt._
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference

import org.scalatest.FunSuite
import xyz.devfortress.splot._

class FigureTest extends FunSuite {
  private def makeG2Capture(): (BufferedImage => Graphics2D, AtomicReference[G2Proxy]) = {
    val g2capture = new AtomicReference[G2Proxy](null)
    val g2c: BufferedImage => Graphics2D  = image => {
      val g2 = new G2Proxy(image.createGraphics())
      g2capture.set(g2)
      g2
    }
    (g2c, g2capture)
  }

  test("Figure with fixed domain and range") {
    val (g2creator, g2capture) = makeG2Capture()
    val fig = Figure(title = "A simple plot",
      titleFont = Font.decode(Font.MONOSPACED),
      domain = (0, 10), range = (0, 5),
      xTicks = Ticks.none, yTicks = Ticks.none,
      g2creator = g2creator
    )
    fig.plot(Seq((0.0, 0.0), (1.0, 1.0), (2.0, 1.5)))
    fig.makeImage(800, 600)

    val graphics2D = g2capture.get()
    assert(graphics2D != null)
    graphics2D.verify() { capturedCalls => {
      import capturedCalls._
      // setting up background for newly created image
      setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      setBackground(Color.WHITE)
      clearRect(0, 0, 800, 600)
      setClip(50, 50, 700, 500)

      // draw two lines of black color and lw=1 corresponding to our plot
      setColor(Color.BLACK)
      getStroke
      setStroke(new BasicStroke(1))
      drawPolyline(Array(50, 120, 190), Array(550, 450, 400), 3)
      setStroke(new BasicStroke(1))

      setClip(null)

      // draw bounding box
      getColor
      getStroke
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
      setFont(Font.decode(Font.MONOSPACED))
      setColor(Color.BLACK)
      getFontMetrics()
      drawString("A simple plot", 354, 35)
      // restore saved font and color
      setColor(Color.BLACK)
      setFont(Font.decode("Dialog-12"))
    }}
  }

  test("Figure with auto domain and range") {
    val (g2creator, g2capture) = makeG2Capture()
    val fig = Figure(
      bgColor = Color.GREEN,
      xTicks = Ticks(0, 2, 4, 6), yTicks = Ticks(1, 3),
      antialiasing = false,
      g2creator = g2creator,
      leftPadding = 5,
      rightPadding = 6,
      topPadding = 7,
      bottomPadding = 8,
      showGrid = true
    )

    fig.scatter(Seq((0, 0), (7, 8)), pt = "o")
    fig.makeImage(800, 600)

    val graphics2D = g2capture.get()
    assert(graphics2D != null)
    for (x <- graphics2D.getCapture()) {
      println(x)
    }

    graphics2D.verify() { capturedCalls => {
      import capturedCalls._
      // setting up background for newly created image
      setBackground(Color.GREEN)
      clearRect(0, 0, 800, 600)
      setClip(5, 7, 789, 585)

      // draw two points
      setColor(Color.BLACK)
      setStroke(new BasicStroke(1))
      drawOval(28,572,3,3)
      drawOval(768,24,3,3)

      // draw bounding box
      setClip(null)
      getColor
      getStroke
      // prepare to draw box
      setColor(Color.BLACK)
      setStroke(new BasicStroke(2))
      drawRect(5, 7, 789, 585)
      // restore saved stroke and color
      setStroke(new BasicStroke(1))
      setColor(Color.BLACK)

      // TODO: Add the the rest of the steps, which are drawing ticks and their labels and then grid
    }}
  }
}
