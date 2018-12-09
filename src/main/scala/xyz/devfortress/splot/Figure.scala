package xyz.devfortress.splot

import java.awt.Color.WHITE
import java.awt.event._
import java.awt.image.BufferedImage
import java.awt.{Font, _}
import java.nio.file.Paths
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.{ConcurrentHashMap, CountDownLatch}

import javax.imageio.ImageIO
import javax.swing.{JFrame, JOptionPane, JPanel}

import scala.collection.mutable
import scala.math.{max, min}

/**
 * Figure class to which plots can be added and later displayed.
 *
 * @param name name of the figure that will appear created window
 * @param bgcolor background color for the entire frame where plot will be displayed.
 * @param leftPadding offset from the left edge of the frame in pixels.
 * @param rightPadding offset from the right edge of the frame in pixels.
 * @param bottomPadding offset from the bottom edge of the frame in pixels.
 * @param topPadding offset from the top edge of the frame in pixels.
 * @param domain range of plot that will be displayed along the x-axis. If None then range will be deprived from the all
 *               added plots.
 * @param range range of plot that will be displayed along the y-axis. If None then range will be deprived from the all
 *              the added plots
 * @param antialiasing boolean value indicating of antialiasing is to be used when rendering plots. By default is is
 *                     true however that can slow down plotting data sets are very large, in which case you may want
 *                     to set this value to false.
 */
class Figure(
      val name: String = "Figure",
      val bgcolor: Color = Color.WHITE,
      val leftPadding: Int = 50,
      val rightPadding: Int = 50,
      val topPadding: Int = 50,
      val bottomPadding: Int = 50,
      val domain: Option[(Double, Double)] = None,
      val range: Option[(Double, Double)] = None,
      val xTicks: (Double, Double) => Seq[(Double, String)] = Ticks.ticks10,
      val yTicks: (Double, Double) => Seq[(Double, String)] = Ticks.ticks10,
      val title: String = "",
      val titleFont: Font = Font.decode("Times-20"),
      val antialiasing: Boolean = true
    ) extends CommonSPlotLibTrait {
  private var currentDomain: (Double, Double) = domain.getOrElse((0, 0))
  private var curentRange: (Double, Double) = range.getOrElse((0, 0))
  private val plots: mutable.MutableList[Plot] = mutable.MutableList()
  private val labels: mutable.MutableList[Label] = mutable.MutableList()
  val currentImage = new AtomicReference[BufferedImage]()
  private val selectingForZoom = new AtomicBoolean(false)
  private val frame: AtomicReference[JFrame] = new AtomicReference[JFrame]()
  private val originalImage = new AtomicReference[BufferedImage](null)
  private val resetP = new AtomicBoolean()
  private var lastFrameDimentions: (Int, Int) = (0, 0)
  private val originalDomain = new AtomicReference[(Double, Double)]()
  private val originalRange = new AtomicReference[(Double, Double)]()
  private val zRanges = new ConcurrentHashMap[Int, (Double, Double)]()
  private val buildingFigure = new CountDownLatch(1)

  /**
   * Add new plot to this figure
   */
  def add(plot: Plot): Unit = plots += plot

  def add(label: Label): Unit = labels += label

  /**
   * Convenience function that can be used instead of fig += LinePlot(...). Adds line plot.
   *
   * @param data  sequence of x-y data points forming plot.
   * @param color color of the line.
   * @param lw    line width.
   */
  def plot[C: ColorLike, D: SeqOfDoubleTuples](data: Seq[D], color: C = Color.BLACK, lw: Int = 1): Unit =
    plots += LinePlot(SeqOfDoubleTuples[D].asDoubleSeq(data), ColorLike[C].asColor(color), lw)

  /**
   * Convenience function that can be used instead of fig += PointPlot(...). Adds scatter plot.
   *
   * @param data  sequence of x-y data points forming plot.
   * @param ps    size of point.
   * @param color color of points.
   * @param pt    type of points.
   */
  def scatter[P: PointTypeLike, C: ColorLike, D: SeqOfDoubleTuples](data: Seq[D], ps: Int = 3, color: C = Color.BLACK,
        pt: P = PointType.Dot): Unit =
    plots += PointPlot(
      SeqOfDoubleTuples[D].asDoubleSeq(data),
      pointSize = ps,
      color = ColorLike[C].asColor(color),
      pointType = PointTypeLike[P].asPointType(pt)
    )

  /**
   * Convenience function that can be used instead of fig += [[ZPointPlot]](...).
   *
   * @param data     sequence of x-y data points forming plot.
   * @param zValues  sequence of z-values for each point in "data" sequence (must be the same size as size of "data").
   * @param ps       point size. Must be greater than 0.
   * @param colorMap color map function that transforms each z-value into color.
   * @param pt       type of data points, i.e. how we display them as dots, crosses or something else.
   * @tparam P type of points.
   */
  def zscatter[P: PointTypeLike, D: SeqOfDoubleTuples](data: Seq[D], zValues: Seq[Double], ps: Int = 5,
    colorMap: Double => Color = colormaps.viridis, pt: P = PointType.Dot): Unit =
    plots += ZPointPlot(
      SeqOfDoubleTuples[D].asDoubleSeq(data),
      zValues,
      pointSize = ps,
      colorMap = colorMap,
      pointType = PointTypeLike[P].asPointType(pt)
    )

  /**
   * Convenience function that can be used instead of fig += [[ZMapPlot]](...) for plotting (x,y)->z function on
   * sub-domain of rectangular x-y domain. By default xDomain, yDomain will be used as a domain of f-function on which
   * it is defined, otherwise if inDomain argument is passed than it is the one that will be used. z-values generated by
   * f(x,y) are converted into color color pixels according colorMap and zRange arguments. By default passed zRange
   * is (0,0) which means that actual zRange will be computed automatically from values of f(x,y) function.
   *
   * @param f        function z=f(x,y).
   * @param xDomain  inclusive domain of x-values on which f(x,y) is defined.
   * @param yDomain  inclusive domain of y-values on which f(x,y) is defined.
   * @param colorMap colormap which transforms each z-value into color; default is viridis.
   * @param zRange   range of z-values produced by f(x,y) function; default range is (0,0) which means that zRange will
   *                 be computed automatically.
   * @param inDomain domain function which can be used to define arbitrary domains in x-y plane where plotting on the
   *                 function f will take place; by default it is assigned to special function "derrivedDomain" which means that it
   *                 will be plotted on xDomain, yDomain rectangle.
   */
  def map(f: (Double, Double) => Double, xDomain: (Double, Double), yDomain: (Double, Double),
    colorMap: Double => Color = colormaps.viridis, zRange: (Double, Double) = (0, 0),
    inDomain: (Double, Double) => Boolean = derivedDomain): Unit =
    plots += ZMapPlot(
      zFunction = f, domain = xDomain, range = yDomain, zRange = zRange, colorMap = colorMap,
      inDomain =
        if (inDomain.equals(derivedDomain))
          (x, y) => xDomain._1 <= x && x <= xDomain._2 && yDomain._1 <= y && y < yDomain._2
        else
          inDomain
    )

  /**
   * Add rectangle.
   *
   * @param anchor    left lower corner of the rectangle.
   * @param width     width of the rectangle.
   * @param height    hight of the rectangle.
   * @param color     color of the lines that form boundaries of this rectangle.
   * @param lw        line with for boundary lines. If line width is set to 0 then only interior of the rectangle is
   *                  shaded with fillColor if defined
   * @param fillColor optional fill color for the interior of the rectangle
   * @param alpha     transparency of the
   */
  def rectangle[C: ColorLike, S: SomethingLikeColor, A](anchor: (A, A), width: Double, height: Double,
    color: C = Color.BLUE,
    lw: Int = 1,
    fillColor: S = Option.empty,
    alpha: Double = 0.2)(implicit integral: Integral[A]): Unit = {
    assert(width > 0, "Width must be greated than 0.")
    assert(height > 0, "Height must be greater than 0.")
    assert(alpha > 0 && alpha <= 1, "Transparency value must be in range (0, 1].")
    val maybeColor = SomethingLikeColor[S].asSomething(fillColor)
    val xAnchor = integral.toDouble(anchor._1)
    val yAnchor = integral.toDouble(anchor._2)
    plots += Shape(
      Seq(
        (xAnchor, yAnchor),
        (xAnchor, yAnchor + height),
        (xAnchor + width, yAnchor + height),
        (xAnchor + width, yAnchor)
      ),
      color = ColorLike[C].asColor(color),
      lineWidth = lw,
      fillColor = maybeColor match {
        case Some(c) => Some(new Color(c.getRed, c.getGreen, c.getBlue, (alpha * 255).toInt))
        case None => None
      }
    )
  }

  /**
   * Display plot window.
   */
  def show(size: (Int, Int) = (1422, 800)): Figure = {
    if (frame.get() == null)
      _show(new Dimension(size._1, size._2))
    buildingFigure.await()
    this
  }

  private def _show(dimension: Dimension): Unit = {
    // set initial value for domain
    currentDomain = domain.getOrElse((plots.map(_.domain._1).min, plots.map(_.domain._2).max))
    curentRange = range.getOrElse((plots.map(_.range._1).min, plots.map(_.range._2).max))

    originalDomain.compareAndSet(null, currentDomain)
    originalRange.compareAndSet(null, curentRange)

    // selections points for rectangle used for zooming
    var selPoint1: java.awt.Point = new java.awt.Point(0, 0)
    var selPoint2: Option[java.awt.Point] = None

    val plotPanel = new JPanel() {
      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)
        val currentFrameDimentions = (getWidth, getHeight)
        if (lastFrameDimentions != currentFrameDimentions) {
          originalImage.set(null) // invalidate original image
          resetP.set(false) // make sure that it is not reset operation
        }
        if (resetP.get() && originalImage.get() != null) {
          resetP.set(false)
          val image = originalImage.get()
          g.drawImage(image, 0, 0, this)

          originalImage.compareAndSet(null, image)
          currentImage.set(image)
          val domainForDisplay = ("%.3G".format(currentDomain._1), "%.3G".format(currentDomain._2))
          val rangeForDisplay = ("%.3G".format(curentRange._1), "%.3G".format(curentRange._2))
          frame.get().setTitle(s"$name domain=$domainForDisplay range=$rangeForDisplay")

        } else if (selectingForZoom.get() && currentImage.get() != null) {
          g.drawImage(currentImage.get(), 0, 0, this)
          // draw zoom in rectangle
          if (selPoint2.isDefined) {
            g.setXORMode(Color.WHITE)
            g.asInstanceOf[Graphics2D].setStroke(new BasicStroke(2))
            g.drawLine(selPoint1.x, selPoint1.y, selPoint1.x, selPoint2.get.y)
            g.drawLine(selPoint1.x, selPoint1.y, selPoint2.get.x, selPoint1.y)
            g.drawLine(selPoint1.x, selPoint2.get.y, selPoint2.get.x, selPoint2.get.y)
            g.drawLine(selPoint2.get.x, selPoint1.y, selPoint2.get.x, selPoint2.get.y)
          }

        } else {
          val domainWidth = currentDomain._2 - currentDomain._1
          val rangeWidth = curentRange._2 - curentRange._1

          val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
          val g2 = image.getGraphics.asInstanceOf[Graphics2D]
          import g2._

          g2.setBackground(bgcolor)
          clearRect(0, 0, getWidth, getHeight)

          if (antialiasing) {
            import java.awt.RenderingHints
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          }

          val plotWidth = getWidth - leftPadding - rightPadding
          val plotHeight = getHeight - bottomPadding - topPadding
          val verticalOffset = topPadding + plotHeight
          val horizontalScaleFactor = plotWidth / domainWidth
          val verticalScaleFactor = plotHeight / rangeWidth

          def xScale(x: Double): Int = (leftPadding + (x - currentDomain._1) * horizontalScaleFactor).toInt

          def yScale(y: Double): Int = (verticalOffset - (y - curentRange._1) * verticalScaleFactor).toInt

          plots.zipWithIndex.foreach(p => {
            val idx = p._2
            p._1 match {
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
              case ZPointPlot(data, zValues, pointSize, colorMap, pointType) =>
                setStroke(new BasicStroke(1))
                val halfSize = pointSize / 2
                data.zip(zValues).foreach(point => {
                  setColor(colorMap(point._2))
                  pointType.drawAt((xScale(point._1._1), yScale(point._1._2)), pointSize, halfSize, g2)
                })
              case ZMapPlot(zFunction, xDomain, yDomain, zRange, colorMap, inDomain) =>
                val iMax = getWidth - rightPadding
                val jMax = getHeight - bottomPadding
                val dx = (currentDomain._2 - currentDomain._1) / (iMax - leftPadding)
                val dy = (curentRange._2 - curentRange._1) / (jMax - topPadding)

                if (zRange._2 - zRange._1 == 0 && !zRanges.contains(idx)) {
                  // we will need to derive zRange
                  val xRange = leftPadding to iMax
                  val yRange = topPadding to jMax
                  // TODO: Reuse "data" for plotting
                  val data: Array[Array[Double]] = Array.ofDim[Double](xRange.size, yRange.size)
                  var minZ = Double.MaxValue
                  var maxZ = Double.MinValue
                  for (i <- leftPadding to iMax; j <- topPadding to jMax) {
                    val x = currentDomain._1 + (i - leftPadding) * dx
                    val y = curentRange._2 - (j - topPadding) * dy
                    if (inDomain(x, y)) {
                      val z = zFunction(x, y)
                      data(i - leftPadding)(j - topPadding) = z
                      if (minZ > z) minZ = z
                      if (maxZ < z) maxZ = z
                    }
                  }
                  zRanges.put(idx, (minZ, maxZ))
                }
                val realZRange = if (zRanges.containsKey(idx))
                  zRanges.get(idx)
                else
                  zRange
                val zLen = realZRange._2 - realZRange._1
                def toUnit(z: Double): Double = (z - realZRange._1) / zLen
                for (i <- leftPadding to iMax; j <- topPadding to jMax) {
                  val x = currentDomain._1 + (i - leftPadding) * dx
                  val y = curentRange._2 - (j - topPadding) * dy
                  if (inDomain(x, y)) {
                    val color = colorMap(toUnit(zFunction(x, y)))
                    image.setRGB(i, j, color.getRGB)
                  }
                }
              case _ => throw new IllegalArgumentException
            }
          })

          // draw white space around display box
          clearRect(0, 0, getWidth, topPadding)
          clearRect(0, 0, leftPadding, getHeight)
          clearRect(0, getHeight - bottomPadding, getWidth, bottomPadding)
          clearRect(getWidth - rightPadding, 0, rightPadding, getHeight)

          // draw bounding box/axis
          setColor(Color.BLACK)
          setStroke(new BasicStroke(2))
          drawRect(leftPadding, topPadding, plotWidth, plotHeight)

          // Draw x-ticks.
          val bottomYPos = plotHeight + topPadding
          for (tick <- xTicks(currentDomain._1, currentDomain._2)) {
            val xPos = xScale(tick._1)
            val tickYEnd = bottomYPos + 7
            drawLine(xPos, bottomYPos, xPos, tickYEnd)
            drawXCenteredString(g2, tick._2, xPos, tickYEnd + 12)
          }

          // Draw y-ticks.
          val origTransformation = g2.getTransform
          for (tick <- yTicks(curentRange._1, curentRange._2)) {
            val yPos = yScale(tick._1)
            drawLine(leftPadding - 7, yPos, leftPadding, yPos)
            g2.rotate(-Math.PI / 2, leftPadding - 12, yPos)
            drawXCenteredString(g2, tick._2, leftPadding - 12, yPos)
            g2.setTransform(origTransformation)
          }

          // Draw labels.
          for (label <- labels) {
            label.draw(g2, (xScale(label.x), yScale(label.y)))
          }

          // Draw title if any
          if (title != "") {
            val savedFont = g2.getFont
            g2.setFont(titleFont)
            val fontMetrics = g2.getFontMetrics()
            g2.drawString(
              title,
              (plotWidth + leftPadding + rightPadding - fontMetrics.stringWidth(title)) / 2,
              topPadding - fontMetrics.getHeight
            )
            g2.setFont(savedFont)
          }

          currentImage.set(image)
          g.drawImage(image, 0, 0, this)

          if (curentRange == originalRange.get() && currentDomain == originalDomain.get()) {
            originalImage.compareAndSet(null, image)
          }
          lastFrameDimentions = (getWidth, getHeight)
          val domainForDisplay = ("%.3G".format(currentDomain._1), "%.3G".format(currentDomain._2))
          val rangeForDisplay = ("%.3G".format(curentRange._1), "%.3G".format(curentRange._2))
          frame.get().setTitle(s"$name domain=$domainForDisplay range=$rangeForDisplay")
        }
        if (buildingFigure.getCount > 0) {
          buildingFigure.countDown()
        }
      }

      override def getPreferredSize: Dimension = new Dimension(dimension)

      private def drawXCenteredString(g: Graphics2D, text: String, x: Int, y: Int): Unit =
        g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y)
    }

    plotPanel.addMouseListener(new MouseAdapter {
      private val savedDir = new AtomicReference[String]()
      override def mousePressed(e: MouseEvent): Unit = {
        super.mousePressed(e)
        e.getButton match {
          case 1 =>
            selectingForZoom.set(true)
            selPoint1 = e.getPoint
          case 3 =>
            val menu = new PlotPopUpMenu(plotPanel, Figure.this, savedDir)
            menu.show(e.getComponent, e.getX, e.getY)
          case _ => // NOOP
        }
      }

      override def mouseReleased(e: MouseEvent): Unit = {
        super.mouseReleased(e)
        if (e.getButton == 1 && selPoint2.isDefined) {
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
          selectingForZoom.set(false)
          plotPanel.repaint()
        }
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
        val theFrame = new JFrame(name)
        frame.set(theFrame)
        theFrame.addKeyListener(new KeyAdapter {
          override def keyPressed(e: KeyEvent): Unit = {
            e.getKeyChar match {
              case 'q' => theFrame.dispose() // close whole window on just pressing 'q'
              case 'r' => // reset zoom
                currentDomain = domain.getOrElse((plots.map(_.domain._1).min, plots.map(_.domain._2).max))
                curentRange = range.getOrElse((plots.map(_.range._1).min, plots.map(_.range._2).max))
                resetP.set(true)
                plotPanel.repaint()
              case _ => // NOOP
            }
          }
        })
        theFrame.setPreferredSize(dimension)
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        theFrame.getContentPane.add(plotPanel)
        theFrame.pack()
        theFrame.setLocationByPlatform(true)
        theFrame.setVisible(true)
      }
    })
  }

  def save(file: String): Figure = {
    val saveToFilePath = Paths.get(file)
    val image = currentImage.get()
    if (saveToFilePath.toString.endsWith(".png")) {
      ImageIO.write(image, "png", saveToFilePath.toFile)
    } else if(saveToFilePath.toString.endsWith(".jpg")) {
      ImageIO.write(image, "jpg", saveToFilePath.toFile)
    } else {
      throw new IllegalArgumentException(s"Unrecognized image file type in $file")
    }
    this
  }

  def close(): Unit = {
    val theFrame = frame.get()
    if (theFrame != null) {
      theFrame.dispatchEvent(new KeyEvent(
        theFrame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, 81, 'q', KeyEvent.KEY_LOCATION_STANDARD
      ))
    }
  }
}

import javax.swing.{JMenuItem, JPopupMenu}

class PlotPopUpMenu(parent: JPanel, figure: Figure, savedDir: AtomicReference[String]) extends JPopupMenu {
  private val saveFile = new JMenuItem("Save as image file")
  saveFile.addActionListener(_ => {
    import javax.swing.JFileChooser
    val fileChooser = new JFileChooser(savedDir.get())
    fileChooser.setDialogTitle("Specify a file to save")

    val userSelection = fileChooser.showSaveDialog(parent)

    if (userSelection == JFileChooser.APPROVE_OPTION) {
      val saveToFilePath = Paths.get(fileChooser.getSelectedFile.getAbsolutePath)
      savedDir.set(saveToFilePath.getParent.toAbsolutePath.toString)
      if (saveToFilePath.toString.endsWith(".png"))
        ImageIO.write(figure.currentImage.get(), "png", saveToFilePath.toFile)
      else if(saveToFilePath.toString.endsWith(".jpg"))
        ImageIO.write(figure.currentImage.get(), "jpg", saveToFilePath.toFile)
      else
        JOptionPane.showMessageDialog(
          parent, "The only supported file formats for saving are .png and .jpg",
          "Error", JOptionPane.ERROR_MESSAGE
        )
    }
  })
  add(saveFile)

  private val resetZoom = new JMenuItem("Reset zoom (r)")
  resetZoom.addActionListener(_ => {
    val frame = parent.getParent.getParent.getParent.getParent.asInstanceOf[JFrame]
    frame.dispatchEvent(new KeyEvent(
      frame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),0, 81, 'r', KeyEvent.KEY_LOCATION_STANDARD
    ))
  })
  add(resetZoom)

  private val closeWindow = new JMenuItem("Close this window (q)")
  closeWindow.addActionListener(_ => {
    val frame = parent.getParent.getParent.getParent.getParent.asInstanceOf[JFrame]
    frame.dispatchEvent(new KeyEvent(
      frame, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),0, 81, 'q', KeyEvent.KEY_LOCATION_STANDARD
    ))
  })
  add(closeWindow)

  addSeparator()

  private val about = new JMenuItem("About")
  about.addActionListener(_ =>
    JOptionPane.showMessageDialog(
      parent, s"SPlot (Scala Plotting)\nVersion: ${getClass.getPackage.getImplementationVersion}",
      "About", JOptionPane.INFORMATION_MESSAGE
    )
  )
  add(about)
}

object Figure {
  def apply(name: String = "Figure",
            bgcolor: Color = WHITE,
            leftPadding: Int = 50,
            rightPadding: Int = 50,
            bottomPadding: Int = 50,
            topPadding: Int = 50,
            domain: Option[(Double, Double)] = None,
            range: Option[(Double, Double)] = None,
            xTicks: (Double, Double) => Seq[(Double, String)] = Ticks.ticks10,
            yTicks: (Double, Double) => Seq[(Double, String)] = Ticks.ticks10,
            title: String = "Figure",
            titleFont: Font = Font.decode("Times-20"),
            antialiasing: Boolean = true)
           (implicit range2OptionD: ((Double, Double)) => Option[(Double, Double)],
            range2OptionI: ((Int, Int)) => Option[(Double, Double)]): Figure =
    new Figure(name, bgcolor, leftPadding, rightPadding, topPadding, bottomPadding, domain, range, xTicks, yTicks,
      antialiasing = antialiasing, title = title, titleFont = titleFont
    )
}
