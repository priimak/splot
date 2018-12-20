package xyz.devfortress.splot

import java.awt.{BasicStroke, Color, Stroke}

object Border {
  val DEFAULT_BORDER_PLOTTER: (DrawingContext, Color) => Unit = borderPlotter()

  def borderPlotter(color: Color = Color.BLACK, stroke: Stroke = new BasicStroke(2), bg2c: Color => Color = c => c)
      (ctx: DrawingContext, bgColor: Color): Unit = {
    import ctx._

    val savedColor = g2.getColor
    val savedStroke = g2.getStroke

    // draw frame rectangle
    g2.setColor(color)
    g2.setStroke(stroke)

    g2.drawRect(leftPadding, topPadding, drawingAreaWidth, drawingAreaHeight)

    g2.setStroke(savedStroke)
    g2.setColor(savedColor)
  }
}
