package xyz.devfortress.splot

import java.awt.{BasicStroke, Color, Stroke}

object Border {
  val DEFAULT_BORDER_PLOTTER: DrawingContext => Unit = borderPlotter()

  def borderPlotter(color: Color = Color.BLACK, stroke: Stroke = new BasicStroke(2))(ctx: DrawingContext): Unit = {
    import ctx._

    val plotWidth = imageWidth - leftPadding - rightPadding
    val plotHeight = imageHeight - bottomPadding - topPadding

    val savedColor = g2.getColor
    val savedStroke = g2.getStroke

    g2.setColor(color)
    g2.setStroke(stroke)

    g2.drawRect(leftPadding, topPadding, plotWidth, plotHeight)

    g2.setStroke(savedStroke)
    g2.setColor(savedColor)
  }
}
