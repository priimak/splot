package xyz.devfortress.splot

import java.awt.Color.WHITE
import java.awt.event.{KeyAdapter, KeyEvent, MouseAdapter, MouseEvent, MouseMotionAdapter}
import java.awt.{BasicStroke, Color, Dimension, Graphics, Graphics2D}

import javax.swing.JPanel

import scala.collection.mutable
import scala.math.{max, min}

/**
 * Figure class to which plots can be added and later displayed.
 *
 * @param bgcolor background color for the entire frame where plot will be displayed.
 * @param leftPadding offset from the left edge of the frame in pixels.
 * @param rightPadding offset from the right edge of the frame in pixels.
 * @param bottomPadding offset from the bottom edge of the frame in pixels.
 * @param topPadding offset from the top edge of the frame in pixels.
 * @param domain range of plot that will be displayed along the x-axis. If None then range will be deprived from the all
 *               added plots.
 * @param range
 */
class Figure(
      bgcolor: Color = Color.WHITE,
      leftPadding: Int = 50,
      rightPadding: Int = 50,
      bottomPadding: Int = 50,
      topPadding: Int = 50,
      domain: Option[(Double, Double)] = None,
      range: Option[(Double, Double)] = None
    ) extends CommonSPlotLibTrait {
  private var currentDomain: (Double, Double) = domain.getOrElse((0, 0))
  private var curentRange: (Double, Double) = range.getOrElse((0, 0))
  private val plots: mutable.MutableList[Plot] = mutable.MutableList()

  /**
   * Add new plot to this figure
   */
  def +=(plot: Plot): Figure = {
    plots += plot
    this
  }

  /**
   * Convenience function that can be used instead of fig += LinePlot(...). Adds line plot.
   *
   * @param data sequence of x-y data points forming plot.
   * @param color color of the line.
   * @param lw line width.
   */
  def plot[C: ColorLike](data: Seq[Point], color: C = Color.BLUE, lw: Int = 1): Figure = {
    plots += LinePlot(data, ColorLike[C].asColor(color), lw)
    this
  }

  /**
   * Convenience function that be used instead of fig += PointPlot(...). Adds scatter plot.
   *
   * @param data sequence of x-y data points forming plot.
   * @param ps size of point
   * @param color color of points
   * @param pt type of points
   */
  def scatter[P: PointTypeLike, C: ColorLike](data: Seq[Point], ps: Int = 3,
                                              color: C = Color.BLUE, pt: P = PointType.Dot): Figure = {
    plots += PointPlot(
      data,
      pointSize = ps,
      color = ColorLike[C].asColor(color),
      pointType = PointTypeLike[P].asPointType(pt)
    )
    this
  }

  /**
   * Add rectangle.
   *
   * @param anchor left lower corner of the rectangle.
   * @param width width of the rectangle.
   * @param height hight of the rectangle.
   * @param color color of the lines that form boundaries of this rectangle.
   * @param lw line with for boundary lines. If line width is set to 0 then only interior of the rectangle is
   *           shaded with fillColor if defined
   * @param fillColor optional fill color for the interior of the rectangle
   * @param alpha transparency of the
   */
  def rectangle[C: ColorLike, S: SomethingLikeColor](anchor: Point, width: Double, height: Double,
                color: C = Color.BLUE,
                lw: Int = 1,
                fillColor: S = Option.empty,
                alpha: Double = 0.2): Figure = {
    assert(width > 0, "Width must be greated than 0.")
    assert(height > 0, "Height must be greater than 0.")
    assert(alpha > 0 && alpha <= 1, "Transparency value must be in range (0, 1].")
    val maybeColor = SomethingLikeColor[S].asSomething(fillColor)
    plots += Shape(
      Seq(
        anchor,
        (anchor._1, anchor._2 + height),
        (anchor._1 + width, anchor._2 + height),
        (anchor._1 + width, anchor._2)
      ),
      color = ColorLike[C].asColor(color),
      lineWidth = lw,
      fillColor = maybeColor match {
        case Some(c) => Some(new Color(c.getRed, c.getGreen, c.getBlue, (alpha * 255).toInt))
        case None => None
      }
    )
    this
  }

  def show(): Unit = {
    // set initial value for domain
    currentDomain = domain.getOrElse((plots.map(_.domain._1).min, plots.map(_.domain._2).max))
    curentRange = range.getOrElse((plots.map(_.range._1).min, plots.map(_.range._2).max))

    // selections points for rectangle used for zooming
    var selPoint1: java.awt.Point = new java.awt.Point(0, 0)
    var selPoint2: Option[java.awt.Point] = None

    val plotPanel = new JPanel() {
      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)

        val domainWidth = currentDomain._2 - currentDomain._1
        val rangeWidth = curentRange._2 - curentRange._1

        val g2 = g.asInstanceOf[Graphics2D]
        import g2._

        g2.setBackground(bgcolor)
        clearRect(0, 0, getWidth, getHeight)

        //import java.awt.RenderingHints // Should I make it optional?
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val plotWidth = getWidth - leftPadding - rightPadding
        val plotHeight = getHeight - bottomPadding - topPadding
        val verticalOffset = topPadding + plotHeight
        val horizontalScaleFactor = plotWidth / domainWidth
        val verticalScaleFactor = plotHeight / rangeWidth

        def xScale(x: Double): Int = (leftPadding + (x - currentDomain._1) * horizontalScaleFactor).toInt
        def yScale(y: Double): Int = (verticalOffset - (y - curentRange._1) * verticalScaleFactor).toInt

        plots.foreach {
          case LinePlot(data, color, lineWidth) =>
            setColor(color)
            setStroke(new BasicStroke(lineWidth))
            data.zip(data.tail).foreach(p2 => {
              drawLine(xScale(p2._1._1), yScale(p2._1._2), xScale(p2._2._1), yScale(p2._2._2))
            })
          case PointPlot(data, pointSize, color, pointType) =>
            setColor(color)
            setStroke(new BasicStroke(1))
            val halfSize = pointSize / 2
            data.foreach(point => {
              pointType.drawAt((xScale(point._1), yScale(point._2)), pointSize, halfSize, g2)
            })
          case Shape(data, color, lineWidth, fillColor) =>
            val onScreenXY = data.map(xy => (xScale(xy._1), yScale(xy._2))).unzip
            if (fillColor.isDefined) {
              setColor(fillColor.get)
              fillPolygon(onScreenXY._1.toArray, onScreenXY._2.toArray, data.size)
            }
            if (lineWidth > 0) {
              setColor(color)
              setStroke(new BasicStroke(lineWidth))
              drawPolygon(onScreenXY._1.toArray, onScreenXY._2.toArray, data.size)
            }
          case _ => throw new IllegalArgumentException
        }

        // draw white space around display box
        clearRect(0, 0, getWidth, topPadding)
        clearRect(0, 0, leftPadding, getHeight)
        clearRect(0, getHeight - bottomPadding, getWidth, bottomPadding)
        clearRect(getWidth - rightPadding, 0, rightPadding, getHeight)

        // draw bounding box/axis
        setColor(Color.BLACK)
        setStroke(new BasicStroke(2))
        drawRect(leftPadding, topPadding, plotWidth, plotHeight)

        // draw zoom in rectangle
        if (selPoint2.isDefined) {
          drawLine(selPoint1.x, selPoint1.y, selPoint1.x, selPoint2.get.y)
          drawLine(selPoint1.x, selPoint1.y, selPoint2.get.x, selPoint1.y)
          drawLine(selPoint1.x, selPoint2.get.y, selPoint2.get.x, selPoint2.get.y)
          drawLine(selPoint2.get.x, selPoint1.y, selPoint2.get.x, selPoint2.get.y)
        }
      }

      override def getPreferredSize: Dimension = new Dimension(1422, 800)
    }

    plotPanel.addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        super.mousePressed(e)
        selPoint1 = e.getPoint
      }

      override def mouseReleased(e: MouseEvent): Unit = {
        super.mouseReleased(e)

        // update range and domain and repaint
        val newDomain1 = (selPoint1.x - leftPadding.toDouble) / (plotPanel.getWidth - leftPadding - rightPadding) *
          (currentDomain._2 - currentDomain._1) + currentDomain._1
        val newDomain2 = (e.getX - leftPadding.toDouble) / (plotPanel.getWidth - leftPadding - rightPadding) *
          (currentDomain._2 - currentDomain._1) + currentDomain._1

        val newRange1 = (plotPanel.getHeight - selPoint1.y - bottomPadding.toDouble) /
          (plotPanel.getHeight - bottomPadding - topPadding) * (curentRange._2 - curentRange._1) + curentRange._1
        val newRange2 = (plotPanel.getHeight - e.getY - bottomPadding.toDouble) /
          (plotPanel.getHeight - bottomPadding - topPadding) * (curentRange._2 - curentRange._1) + curentRange._1

        currentDomain = (min(newDomain1, newDomain2), max(newDomain1, newDomain2))
        curentRange = (min(newRange1, newRange2), max(newRange1, newRange2))

        selPoint2 = None
        plotPanel.repaint()
      }
    })
    plotPanel.addMouseMotionListener(new MouseMotionAdapter {
      override def mouseDragged(e: MouseEvent): Unit = {
        super.mouseDragged(e)
        selPoint2 = Some(e.getPoint)
        plotPanel.repaint()
      }
    })

    import javax.swing.SwingUtilities
    SwingUtilities.invokeLater(new Runnable() {
      override def run(): Unit = {
        import javax.swing.JFrame
        val frame: JFrame = new JFrame("DrawGraph")
        frame.addKeyListener(new KeyAdapter {
          override def keyPressed(e: KeyEvent): Unit = {
            if (e.getKeyChar == 'q') frame.dispose() // close whole window on just pressing 'q'
            if (e.getKeyChar == 'r') {
              currentDomain = domain.getOrElse((plots.map(_.domain._1).min, plots.map(_.domain._2).max))
              curentRange = range.getOrElse((plots.map(_.range._1).min, plots.map(_.range._2).max))
              plotPanel.repaint()
            }
          }
        })
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.getContentPane.add(plotPanel)
        frame.pack()
        frame.setLocationByPlatform(true)
        frame.setVisible(true)
      }
    })
  }
}

object Figure {
  def apply(bgcolor: Color = WHITE,
            leftPadding: Int = 50,
            rightPadding: Int = 50,
            bottomPadding: Int = 50,
            topPadding: Int = 50,
            domain: Option[(Double, Double)] = None,
            range: Option[(Double, Double)] = None)
           (implicit range2OptionD: ((Double, Double)) => Option[(Double, Double)],
            range2OptionI: ((Int, Int)) => Option[(Double, Double)]): Figure =
    new Figure(bgcolor, leftPadding, rightPadding, bottomPadding, topPadding, domain, range)
}
