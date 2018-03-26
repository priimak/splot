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
   * @return Primary color for this plot
   */
  def color: Color

  /**
   * Sequence of points that defines this plot
   */
  def data: Seq[Point]
}

abstract class PlotBase extends Plot {
  override val domain: (Double, Double) =
    data.foldLeft((data.head._1, data.head._1))(
      (acc, v) => (min(acc._1, v._1), max(acc._2, v._1))
    )

  override val range: (Double, Double) =
    data.foldLeft((data.head._2, data.head._2))(
      (acc, v) => (min(acc._1, v._2), max(acc._2, v._2))
    )
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