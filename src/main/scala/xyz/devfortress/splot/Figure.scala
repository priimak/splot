package xyz.devfortress.splot

import java.awt.event._
import java.awt.image.BufferedImage
import java.awt.{Font, _}
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import javax.imageio.ImageIO
import javax.swing.{JFrame, JOptionPane, JPanel}

import scala.collection.mutable
import scala.math.{max, min}

/**
 * Figure class to which plots can be added and later displayed.
 *
 * @param name name of the figure that will appear created window
 * @param bgColor background color for the entire frame where plot will be displayed.
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
case class Figure(
      name: String = "Figure",
      title: String = "",
      titleFont: Font = Font.decode("Times-20"),
      xLabel: String = "",
      yLabel: String = "",
      bgColor: Color = Color.WHITE,
      leftPadding: Int = 50,
      rightPadding: Int = 50,
      topPadding: Int = 50,
      bottomPadding: Int = 50,
      antialiasing: Boolean = true,
      showGrid: Boolean = false,
      domain: Option[(Double, Double)] = None,
      range: Option[(Double, Double)] = None,
      xTicks: (Double, Double) => Seq[(Double, String)] = Ticks.ticks10,
      yTicks: (Double, Double) => Seq[(Double, String)] = Ticks.ticks5,
      backgroundPlotter: (DrawingContext, Color) => Unit = Background.DEFAULT_BACKGROUND_PLOTTER,
      gridPlotter: (DrawingContext, Seq[Int], Seq[Int]) => Unit = Grid.DEFAULT_GRID_PLOTTER,
      xLabelPlotter: (DrawingContext, Int, String) => Unit = XYLabels.DEFAULT_XLABEL_PLOTTER,
      yLabelPlotter: (DrawingContext, Int, String) => Unit = XYLabels.DEFAULT_YLABEL_PLOTTER,
      borderPlotter: (DrawingContext, Color) => Unit = Border.DEFAULT_BORDER_PLOTTER,
      titlePlotter: (DrawingContext, String, Font) => Unit = Title.DEFAULT_TITLE_PLOTTER,
      xTicksPlotter: (DrawingContext, Seq[(Double, String)]) => Int = Ticks.DEFAULT_XTICKS_PLOTTER,
      yTicksPlotter: (DrawingContext, Seq[(Double, String)]) => Int = Ticks.DEFAULT_YTICKS_PLOTTER,
      g2creator: BufferedImage => Graphics2D = image => image.createGraphics() // exposed for testing
    ) extends CommonSPlotLibTrait {
  private var currentDomain: (Double, Double) = domain.getOrElse((0, 0))
  private var currentRange: (Double, Double) = range.getOrElse((0, 0))
  private val plotElements: mutable.MutableList[PlotElement] = mutable.MutableList()
  val currentImage = new AtomicReference[BufferedImage]()
  private val selectingForZoom = new AtomicBoolean(false)
  private val frame: AtomicReference[JFrame] = new AtomicReference[JFrame]()
  private val originalImage = new AtomicReference[BufferedImage](null)
  private val resetP = new AtomicBoolean()
  private var lastFrameDimensions: (Int, Int) = (0, 0)
  private val originalDomain = new AtomicReference[(Double, Double)]()
  private val originalRange = new AtomicReference[(Double, Double)]()
  private val buildingFigure = new CountDownLatch(1)

  /**
   * Add new plot to this figure
   */
  def add(plotElement: PlotElement): Unit = plotElements += plotElement

  /**
   * Convenience function that can be used instead of fig += LinePlot(...). Adds line plot.
   *
   * @param data  sequence of x-y data points forming plot.
   * @param color color of the line.
   * @param lw    line width.
   */
  def plot[C: ColorLike, L: LinesTypeLike](
      data: Seq[(Double, Double)], color: C = Color.BLACK, lw: Int = 1, lt: L = LineType.SOLID): Unit =
    plotElements += LinePlot(
      data = data,
      color = ColorLike[C].asColor(color),
      lineWidth = lw,
      lineType = LinesTypeLike[L].asLineType(lt)
    )

  /**
   * Convenience function that can be used instead of fig += PointPlot(...). Adds scatter plot.
   *
   * @param data  sequence of x-y data points forming plot.
   * @param ps    size of point.
   * @param color color of points.
   * @param fc    fill color
   * @param fa    fill alpha from 0 to 1 (default)
   * @param pt    type of points.
   */
  def scatter[P: PointTypeLike, C: ColorLike, S: SomethingLikeColor](
      data: Seq[(Double, Double)], ps: Int = 3, color: C = Color.BLACK, fc: S = Option.empty, fa: Double = 1.0, pt: P = PointType.Dot): Unit = {
    assert(fa > 0 && fa <= 1, "Transparency value must be in range (0, 1].")
    val pointType = PointTypeLike[P].asPointType(pt)
    val c = ColorLike[C].asColor(color)
    val fillColor: Option[Color] = pointType match {
      case PointType.Dot => Some(new Color(c.getRed, c.getRed, c.getBlue, (fa * 255).toInt))
      case _ => SomethingLikeColor[S].asSomething(fc)
        .flatMap(c => Some(new Color(c.getRed, c.getGreen, c.getBlue, (fa * 255).toInt)))
    }
    plotElements += PointPlot(
      data,
      pointSize = ps,
      color = ColorLike[C].asColor(color),
      pointType = PointTypeLike[P].asPointType(pt),
      fillColor = fillColor
    )
  }

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
  def zscatter[P: PointTypeLike](data: Seq[(Double, Double)], zValues: Seq[Double], ps: Int = 5,
    colorMap: Double => Color = colormaps.viridis, pt: P = PointType.Dot): Unit =
    plotElements += ZPointPlot(
      data,
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
    plotElements += ZMapPlot(
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
  def rectangle[C: ColorLike, S: SomethingLikeColor, A: Integral, L: LinesTypeLike](
      anchor: (A, A),
      width: Double,
      height: Double,
      color: C = Color.BLUE,
      lw: Int = 1,
      lt: L = LineType.SOLID,
      fillColor: S = Option.empty,
      alpha: Double = 1.0)(implicit integral: Integral[A]): Unit = {
    assert(width > 0, "Width must be greater than 0.")
    assert(height > 0, "Height must be greater than 0.")
    assert(alpha > 0 && alpha <= 1, "Transparency value must be in range (0, 1].")
    val maybeColor = SomethingLikeColor[S].asSomething(fillColor)
    val xAnchor = integral.toDouble(anchor._1)
    val yAnchor = integral.toDouble(anchor._2)
    plotElements += makeRectangle(
      anchor = (xAnchor, yAnchor),
      width = width, height = height,
      color = ColorLike[C].asColor(color),
      lw = lw,
      lt = LinesTypeLike[L].asLineType(lt),
      fillColor = maybeColor,
      alpha = alpha
    )
  }

  /**
   * Draw sequence of data points as bar plot.
   *
   * @param data Sequence of data points.
   * @param color Fill color for the bars
   * @param edgeColor Color for the edge of the bars.
   * @param edgeWidth Width of the bar edges.
   * @param width Function that defines width of the bar. For each bar it receives distance to the next data point and
   *              returns distance to be used for drawing current bar. Default function is identity, which means that
   *              bars are drawn edge to edge without any space between bars.
   * @param alpha Transparency for the fill color from 0 (fully transparent) to 1 (fully opaque, default value)
   */
  def barplot[C: ColorLike](data: Seq[(Double, Double)], color: C = Color.YELLOW,
      edgeColor: C = Color.BLACK, edgeWidth: Int = 1, width: Double => Double = w => w, alpha: Double = 1.0): Unit = {
    if (data.size > 1) { // draw only if there are at least two data points
      val p2s = data.zip(data.tail)
      val colorOfEdge = ColorLike[C].asColor(edgeColor)
      val colorOfFill = ColorLike[C].asColor(color)
      val boxes: Seq[Left[Shape, Label]] = for (p2 <- p2s) yield {
        Left(makeRectangle(
          anchor = (p2._1._1, 0.0),
          width = width(p2._2._1 - p2._1._1),
          height = p2._1._2,
          color = colorOfEdge,
          lw = edgeWidth,
          lt = LineType.SOLID,
          fillColor = Some(colorOfFill),
          alpha = alpha
        ))
      }

      // last rectangle will be of the same width as the one before last
      val lastPair = p2s.last
      plotElements += CompositePlotElement(boxes ++ Seq(Left(makeRectangle(
        anchor = (lastPair._2._1, 0.0),
        width = width(lastPair._2._1 - lastPair._1._1),
        height = lastPair._2._2,
        color = colorOfEdge,
        lw = edgeWidth,
        lt = LineType.SOLID,
        fillColor = Some(colorOfFill),
        alpha = alpha
      ))))
    }
  }

  private def makeRectangle(anchor: (Double, Double), width: Double, height: Double,
    color: Color, lw: Int, lt: LineType, fillColor: Option[Color], alpha: Double): Shape = {
    assert(width > 0, "Width must be greated than 0.")
    assert(height > 0, "Height must be greater than 0.")
    assert(alpha > 0 && alpha <= 1, "Transparency value must be in range (0, 1].")
    Shape(
      Seq(
        (anchor._1, anchor._2),                  (anchor._1, anchor._2 + height),
        (anchor._1 + width, anchor._2 + height), (anchor._1 + width, anchor._2)
      ),
      color = color,
      lineWidth = lw,
      lineType = lt,
      fillColor = fillColor match {
        case Some(c) => Some(new Color(c.getRed, c.getGreen, c.getBlue, (alpha * 255).toInt))
        case None => None
      }
    )
  }

  /**
   * Create SPlotImage which can later be saved into in a file.
   *
   * @param width width of the image
   * @param height height of the image
   * @return instance of [[SPlotImage]]
   */
  def makeImage(width: Int, height: Int): SPlotImage = {
    _makeImage(width, height, true)
  }

  private def getBoundsFromPlots(bounds: Option[(Double, Double)], extractBound: Plot => (Double, Double))= {
    bounds.getOrElse({
      val elements = plotElements.flatMap({
        case CompositePlotElement(ps) => ps.flatMap({
          case Left(plt) => Seq(plt)
          case Right(_) => Seq()
        })
        case plt: Plot => Seq(plt)
        case _ => Seq()
      })
      val min = elements.map(extractBound(_)._1).min
      val max = elements.map(extractBound(_)._2).max
      // we need to add padding to these exact bounds
      val padding = (max - min) / 30
      (min - padding, max + padding)
    })
  }

  private def _makeImage(imageWidth: Int, imageHeight: Int, setRangeAndDomain: Boolean): SPlotImage = {
    if (setRangeAndDomain) {
      // set initial value for domain
      currentDomain = getBoundsFromPlots(domain, _.domain)
      currentRange = getBoundsFromPlots(range, _.range)
    }

    import java.awt.image.BufferedImage
    val image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
    val g2: Graphics2D = g2creator(image)

    if (antialiasing) {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
      g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    }

    val domainWidth = currentDomain._2 - currentDomain._1
    val rangeWidth = currentRange._2 - currentRange._1
    val plotWidth = imageWidth - leftPadding - rightPadding
    val plotHeight = imageHeight - bottomPadding - topPadding
    val verticalOffset = topPadding + plotHeight
    val horizontalScaleFactor = plotWidth / domainWidth
    val verticalScaleFactor = plotHeight / rangeWidth

    def xScale(x: Double): Int =  Math.round(leftPadding + (x - currentDomain._1) * horizontalScaleFactor).toInt

    def yScale(y: Double): Int = Math.round(verticalOffset - (y - currentRange._1) * verticalScaleFactor).toInt

    val drawingContext = DrawingContext(
      g2, xScale, yScale, imageWidth, imageHeight,
      rightPadding, leftPadding, topPadding, bottomPadding,
      currentDomain, currentRange, image
    )

    backgroundPlotter(drawingContext, bgColor)

    // Draw all plot elements
    plotElements.flatMap(p => p match {
      case CompositePlotElement(plotElementsInComposite) => plotElementsInComposite.map {
        case Left(plt) => plt
        case Right(lbl) => lbl
      }
      case _ => Seq(p)
    }).zipWithIndex.foreach(p => p._1 match {
      case plt: Plot => plt.draw(drawingContext, p._2)
      case label: Label => label.draw(g2, (xScale(label.x), yScale(label.y)))
      case _ => throw new RuntimeException("Recursive use of CompositePlotElement is not supported")
    })

    // Unset any clip area set in background plotter or in plot elements and draw bounding box/axis.
    g2.setClip(null)
    borderPlotter(drawingContext, bgColor)

    // Draw x-ticks.
    val xticks = xTicks(currentDomain._1, currentDomain._2)
    val xTicksVerticalDistance = if (xticks.isEmpty) 0 else xTicksPlotter(drawingContext, xticks)

    // Draw y-ticks.
    val yticks = yTicks(currentRange._1, currentRange._2)
    val yTicksHorizontalDistance = if (yticks.isEmpty) 0 else yTicksPlotter(drawingContext, yticks)

    if (showGrid) {
      gridPlotter(drawingContext, xticks.map(x => xScale(x._1)), yticks.map(y => yScale(y._1)))
    }

    xLabelPlotter(drawingContext, xTicksVerticalDistance, xLabel)
    yLabelPlotter(drawingContext, yTicksHorizontalDistance, yLabel)

    // Draw title
    titlePlotter(drawingContext, title, titleFont)

    new SPlotImage(image)
  }

  private def drawXCenteredString(g: Graphics2D, text: String, x: Int, y: Int): Unit =
    g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y)

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
    // set initial value for domain and range
    currentDomain = getBoundsFromPlots(domain, _.domain)
    currentRange = getBoundsFromPlots(range, _.range)

    originalDomain.compareAndSet(null, currentDomain)
    originalRange.compareAndSet(null, currentRange)

    // selections points for rectangle used for zooming
    var selPoint1: java.awt.Point = new java.awt.Point(0, 0)
    var selPoint2: Option[java.awt.Point] = None

    val plotPanel = new JPanel() {
      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)
        val currentFrameDimentions = (getWidth, getHeight)
        if (lastFrameDimensions != currentFrameDimentions) {
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
          val rangeForDisplay = ("%.3G".format(currentRange._1), "%.3G".format(currentRange._2))
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
          val rangeWidth = currentRange._2 - currentRange._1

          val image = _makeImage(getWidth, getHeight, false).image
          currentImage.set(image)
          g.drawImage(image, 0, 0, this)

          if (currentRange == originalRange.get() && currentDomain == originalDomain.get()) {
            originalImage.compareAndSet(null, image)
          }
          lastFrameDimensions = (getWidth, getHeight)
          val domainForDisplay = ("%.3G".format(currentDomain._1), "%.3G".format(currentDomain._2))
          val rangeForDisplay = ("%.3G".format(currentRange._1), "%.3G".format(currentRange._2))
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
            (plotPanel.getHeight - bottomPadding - topPadding) * (currentRange._2 - currentRange._1) + currentRange._1
          val newRange2 = (plotPanel.getHeight - e.getY - bottomPadding.toDouble) /
            (plotPanel.getHeight - bottomPadding - topPadding) * (currentRange._2 - currentRange._1) + currentRange._1

          currentDomain = (min(newDomain1, newDomain2), max(newDomain1, newDomain2))
          currentRange = (min(newRange1, newRange2), max(newRange1, newRange2))

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
                currentDomain = getBoundsFromPlots(domain, _.domain)
                currentRange = getBoundsFromPlots(range, _.range)
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
