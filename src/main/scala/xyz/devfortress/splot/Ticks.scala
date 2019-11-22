package xyz.devfortress.splot

import java.awt.{BasicStroke, Color, Font, Stroke}

import scala.collection.immutable.NumericRange

/**
 * Class for defining ticks as fixed points. It ignores input arguments to xTicks and/or yTicks returns provided
 * sequence of ticks. For convenience apply methods from companion Ticks object are also provided. There tick labels
 * simply be x or y coordinate of the tick converted into string. You can use them like so
 * {{{
 *   new Figure(xTicks = Ticks(1 to 10), yTicks = Ticks(0.12, 0.24, 0.48, 0.96))
 * }}}
 *
 * To disable ticks drawing construct empty Ticks object or use predefined function that always returns empty
 * sequence of ticks like so
 *
 * {{{
 *   new Figure(xTicks = Ticks(), yTicks = Ticks.none)
 * }}}
 *
 * @param ticks Given ticks to be returned by x/yTicks function.
 */
class Ticks(ticks: Seq[(Double, String)]) extends ((Double, Double) => Seq[(Double, String)]) {
  override def apply(min: Double, max: Double): Seq[(Double, String)] =
    ticks.filter(_._1 >= min).filter(_._1 <= max)
}

object Ticks {
  val DEFAULT_XTICKS_PLOTTER: (DrawingContext, Seq[(Double, String)]) => Int = xTicksPlotter()
  val DEFAULT_YTICKS_PLOTTER: (DrawingContext, Seq[(Double, String)]) => Int = yTicksPlotter()

  private def floor(d: Double, decimalPlace: Int) =
    BigDecimal(d).setScale(decimalPlace, BigDecimal.RoundingMode.FLOOR).doubleValue

  private def ceil(d: Double, decimalPlace: Int) =
    BigDecimal(d).setScale(decimalPlace, BigDecimal.RoundingMode.CEILING).doubleValue


  val x: (Double, Double) => Seq[(Double, String)] = ticksN(5)

  /**
   * Function that is passed as xTicks and yTicks parameter to [[Figure]] constructor which will make 10 or so ticks.
   */
  def ticksN(numTicks: Int)(min: Double, max: Double): Seq[(Double, String)] = {
    if (max <= min) {
      Seq()
    } else {
      val d = max - min
      val dPerN = d / numTicks
      val dPerN2 = dPerN / 2

      val proposedTickDistance = Stream.from(1).map(d => floor(dPerN, d)).filter(_ != 0).head

      val (decimalPoints, tickDistance) = (0 to 100)
        .map(x => (x, ceil(proposedTickDistance, x)))
        .filter(_._2 <= dPerN)
        .head

      // Now find position for the first tick.
      val proposedFirsPosition = (min / tickDistance).toInt * tickDistance
      val firstTickPosition =
        if (proposedFirsPosition >= min)
          proposedFirsPosition
        else
          proposedFirsPosition + tickDistance

      // Now we are ready to assemble sequence
      (0 to 20).map(firstTickPosition + tickDistance * _)
        .filter(_ <= max)
        .filter(_ >= min)
        .map(tickPosition => (tickPosition, s"%.${decimalPoints}f".format(tickPosition)))
    }
  }

  val ticks10: (Double, Double) => Seq[(Double, String)] = ticksN(9)_
  val ticks5: (Double, Double) => Seq[(Double, String)] = ticksN(5)_

  val none: (Double, Double) => Seq[(Double, String)] = (_, _) => Seq()

  def apply(ticks: Double*): Ticks = new Ticks(ticks.map(t => (t, s"$t")))

  def apply(ticks: Range): Ticks = new Ticks(ticks.map(t => (t.toDouble, s"$t")))

  def apply(ticks: NumericRange[Double]): Ticks = new Ticks(ticks.map(t => (t, s"$t")))

  /**
   * Curried function for constructing xTickPlotter function that is passed to the [[Figure]] constructor. Pass
   * first 4 arguments and obtain function suitable for passing to [[Figure]] constructor.
   *
   * @param color Color with which ticks and tick-labels will be drawn.
   * @param tickLength Length of the tick in pixels.
   * @param stroke Stroke for drawing tick which is a short line of length tickLength.
   * @param font Font to be used for drawing labels.
   * @param ctx Drawing context.
   * @param ticks Sequence of (x-coordinate, label) that defines ticks along the x-axis.
   * @return Total height in pixels taken by the ticks together with labels if any.
   */
  def xTicksPlotter(
      color: Color = Color.BLACK,
      tickLength: Int = 5,
      stroke: Stroke = new BasicStroke(2),
      font: Font = Font.decode("Monospaced-13"))(ctx: DrawingContext, ticks: Seq[(Double, String)]): Int = {
    import ctx._
    val bottomYPos = imageHeight - bottomPadding


    var extraHeight: Int = 0
    g2.withFont(font)
      .withColor(color)
      .withStroke(stroke)
      .draw { g2 =>
        val metrics = g2.getFontMetrics()
        ticks.foreach {
          case (x, text) =>
            val xPos = x2i(x)
            val tickYEnd = bottomYPos + tickLength
            g2.drawLine(xPos, bottomYPos, xPos, tickYEnd)
            if (!text.isEmpty) {
              g2.drawString(text, xPos - metrics.stringWidth(text) / 2, tickYEnd + metrics.getHeight)
              extraHeight = metrics.getHeight
            }
        }
      }
    tickLength + extraHeight
  }

  /**
   * See comment for Ticks.xTicksPlotter() except for y-axis.
   */
  def yTicksPlotter(
    color: Color = Color.BLACK,
    tickLength: Int = 5,
    stroke: Stroke = new BasicStroke(2),
    font: Font = Font.decode("Monospaced-13"))(ctx: DrawingContext, ticks: Seq[(Double, String)]): Int = {
    import ctx._

    val savedFont = g2.getFont
    val savedStroke = g2.getStroke
    val savedColor = g2.getColor
    val origTransformation = g2.getTransform

    g2.setFont(font)
    g2.setColor(color)
    g2.setStroke(stroke)
    val metrics = g2.getFontMetrics()

    var hasLabels = false
    ticks.foreach {
      case (y, text) =>
        val yPos = y2i(y)
        g2.drawLine(leftPadding - tickLength, yPos, leftPadding, yPos)
        g2.rotate(-Math.PI / 2, leftPadding - metrics.getHeight, yPos)
        if (!text.isEmpty) {
          g2.drawString(text, leftPadding - metrics.getHeight - metrics.stringWidth(text) / 2, yPos)
          hasLabels = true
        }
        g2.setTransform(origTransformation)
    }

    g2.setColor(savedColor)
    g2.setStroke(savedStroke)
    g2.setFont(savedFont)

    tickLength + (if (hasLabels) metrics.getHeight else 0)
  }
}
