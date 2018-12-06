package xyz.devfortress.splot

import java.awt.Color

import scala.math.{max, min}

sealed trait Plot extends CommonSPlotLibTrait {
  /**
    * Domain, i.e. inclusive range along the x-axis.
    */
  def domain: (Double, Double)

  /**
    * Range, i.e. inclusive range along the y-axis.
    */
  def range: (Double, Double)

  /**
   * Domain predicate. Function that will use used to determine if particular point is within x-y domain.
   */
  def inDomain: (Double, Double) => Boolean
}

sealed trait LazyZMapPlot extends Plot {
  /**
    * Function that will be used to display color point for each pixel of the plot that falls into function domain.
    */
  def zFunction: (Double, Double) => Double

  /**
    * Range of Z value as returned by the zFunction. If there z-values that fall outside of this range their color will
    * be clipped.
    */
  def zRange: (Double, Double)

  /**
    * Color map to be used when displaying this heat-map like plot.
    * @return
    */
  def colorMap: Double => Color = colormaps.viridis
}

sealed trait SimplePlot extends Plot {
  /**
   * @return Primary color for this plot
   */
  def color: Color

  /**
   * Sequence of points that defines this plot
   */
  def data: Seq[(Double, Double)]
}

abstract class PlotBase extends SimplePlot {
  override val domain: (Double, Double) =
    data.foldLeft((data.head._1, data.head._1))(
      (acc, v) => (min(acc._1, v._1), max(acc._2, v._1))
    )

  override val range: (Double, Double) =
    data.foldLeft((data.head._2, data.head._2))(
      (acc, v) => (min(acc._1, v._2), max(acc._2, v._2))
    )

  override def inDomain: (Double, Double) => Boolean =
    (x, y) => domain._1 <= x && x <= domain._2 && range._1 <= y && y <= range._2
}

/**
 * Plot shown as set of x-y points (i.e. a scatter plot).
 *
 * @param data sequence of data points.
 * @param pointSize point size. Must be greater than 0.
 * @param color color of the points.
 * @param pointType type of data points, i.e. how we display them as dots, crosses or something else.
 */
final case class PointPlot(
  data: Seq[(Double, Double)],
  pointSize: Int = 3,
  color: Color = Color.BLUE,
  pointType: PointType = PointType.Dot
) extends PlotBase {
  assert(pointSize > 0)
}

/**
 * ZPoint plot is very similar to PointPlot but also contains sequence of zValues that should be in range from 0 to 1
 * inclusive and color of each point is determined by by calling provided colorMap function which takes value from
 * zValues for a given point and returns color to be used for plotting this point.
 *
 * @param data sequence of data points.
 * @param zValues sequence of z-values for each point in "data" sequence (must be the same size as size of "data").
 * @param pointSize point size. Must be greater than 0.
 * @param colorMap color map function that transforms each z-value into color.
 * @param pointType type of data points, i.e. how we display them as dots, crosses or something else.
 */
final case class ZPointPlot(
  data: Seq[(Double, Double)],
  zValues: Seq[Double],
  pointSize: Int = 5,
  colorMap: Double => Color = colormaps.viridis,
  pointType: PointType = PointType.Dot
) extends PlotBase {
  assert(pointSize > 0)
  assert(data.size == zValues.size)

  /**
   * Primary color for this plot is ignored
   */
  override def color: Color = Color.BLACK
}

/**
 * Plot shown as set of x-y points (i.e. a scatter plot).
 *
 * @param data sequence of data points.
 * @param color color of the lines.
 * @param lineWidth width of the line in pixels. Must be greater than 0.
 */
final case class LinePlot(
  data: Seq[(Double, Double)],
  color: Color = Color.BLUE,
  lineWidth: Int = 1
) extends PlotBase {
 assert(lineWidth > 0)
}

/**
 * Plot shown as a closed polygon shape.
 *
 * @param data sequence of data points that define vertices of this polygon.
 * @param color color of lines that are drawn at the polygon boundaries. Default value is BLUE.
 * @param lineWidth width of the lines drawn at the polygon boundaries. Must be greater or equals to 0. If lineWidth
 *                  is 0 then only interior of the polygon is drawn/shaded using value of fillColor.
 * @param fillColor color to use to shade interior of the polygon. If fillColor is None (default) then interior of the
 *                  polygon is not shaded.
 */
final case class Shape(
  override val data: Seq[(Double, Double)],
  override val color: Color = Color.BLUE,
  lineWidth: Int = 1,
  fillColor: Option[Color] = None
) extends PlotBase {
  assert(lineWidth >= 0, "Line width might be greater or equals to 0")
}

/**
 * Simple rectangular heat map plot of certain zFunction that maps points in x,y plane into z-values.
 */
final case class ZMapPlot(
  zFunction: (Double, Double) => Double,
  override val domain: (Double, Double),
  override val range: (Double, Double),
  zRange: (Double, Double) = (0, 0),
  colorMap: Double => Color = colormaps.viridis,
  override val inDomain: (Double, Double) => Boolean
) extends Plot
