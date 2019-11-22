package xyz.devfortress.splot

import java.awt.{Color, Font}

object XYLabels {
  val DEFAULT_XLABEL_PLOTTER: (DrawingContext, Int, String) => Unit = xLabelPlotter()
  val DEFAULT_YLABEL_PLOTTER: (DrawingContext, Int, String) => Unit = yLabelPlotter()

  def xLabelPlotter(font: Font = Font.decode("Monospaced-15"), color: Color = Color.BLACK)
      (ctx: DrawingContext, ticksHeight: Int, label: String): Unit = {
    if (label.nonEmpty) {
      import ctx._

      g2.withFont(font)
        .withColor(color)
        .draw { g2 =>
          val metrics = g2.getFontMetrics
          val x = (imageWidth - metrics.stringWidth(label)) / 2
          val y = imageHeight - bottomPadding + ticksHeight + metrics.getHeight

          g2.drawString(label, x, y)
        }
    }
  }

  def yLabelPlotter(font: Font = Font.decode("Monospaced-15"), color: Color = Color.BLACK)
    (ctx: DrawingContext, ticksWidth: Int, label: String): Unit = {
    if (label.nonEmpty) {
      import ctx._

      val metrics = g2.getFontMetrics
      val halfLabelLen = metrics.stringWidth(label) / 2
      val x = (leftPadding - ticksWidth - metrics.getHeight * 1.2 - halfLabelLen).toInt
      val y = (imageHeight + metrics.getHeight) / 2

      g2.withFont(font)
        .withColor(color)
        .withTransform(-Math.PI / 2, x + halfLabelLen, imageHeight / 2)
        .draw(_.drawString(label, x, y))
    }
  }
}
