package xyz.devfortress.splot

import java.awt.Color

object Background {
  val DEFAULT_BACKGROUND_PLOTTER: (DrawingContext, Color) => Unit = backgroundPlotter

  def backgroundPlotter(ctx: DrawingContext, color: Color): Unit = {
    import ctx._
    g2.setBackground(color)
    g2.clearRect(0, 0, imageWidth, imageHeight)
    g2.setClip(leftPadding, topPadding, drawingAreaWidth, drawingAreaHeight)
  }
}
