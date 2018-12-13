package xyz.devfortress.splot

import java.awt.{BasicStroke, Color, Stroke}

object Border {
  val DEFAULT_BORDER_PLOTTER: (DrawingContext, Color) => Unit = borderPlotter()

  def borderPlotter(color: Color = Color.BLACK, stroke: Stroke = new BasicStroke(2), bg2c: Color => Color = c => c)
      (ctx: DrawingContext, bgColor: Color): Unit = {
    import ctx._

    val plotWidth = imageWidth - leftPadding - rightPadding
    val plotHeight = imageHeight - bottomPadding - topPadding

    val savedBackgroundColor = g2.getBackground
    val savedColor = g2.getColor
    val savedStroke = g2.getStroke

    // Draw white space around display box.
    g2.setBackground(bg2c(bgColor))
    g2.clearRect(0, 0, imageWidth, topPadding)
    g2.clearRect(0, 0, leftPadding, imageHeight)
    g2.clearRect(0, imageHeight - bottomPadding, imageWidth, bottomPadding)
    g2.clearRect(imageWidth - rightPadding, 0, rightPadding, imageHeight)
    g2.setBackground(savedBackgroundColor)

    // draw frame rectangle
    g2.setColor(color)
    g2.setStroke(stroke)

    g2.drawRect(leftPadding, topPadding, plotWidth, plotHeight)

    g2.setStroke(savedStroke)
    g2.setColor(savedColor)
  }
}
