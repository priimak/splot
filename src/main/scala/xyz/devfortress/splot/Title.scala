package xyz.devfortress.splot

import java.awt.{Color, Font}

object Title {
  val DEFAULT_TITLE_PLOTTER: (DrawingContext, String, Font) => Unit = titlePlotter()

  def titlePlotter(color: Color = Color.BLACK)(ctx: DrawingContext, text: String, font: Font): Unit = {
    import ctx._

    if (text != "") {
      val plotWidth = imageWidth - leftPadding - rightPadding

      val savedFont = g2.getFont
      val savedColor = g2.getColor

      g2.setFont(font)
      g2.setColor(color)

      val fontMetrics = g2.getFontMetrics()
      g2.drawString(
        text,
        (plotWidth + leftPadding + rightPadding - fontMetrics.stringWidth(text)) / 2,
        topPadding - fontMetrics.getHeight
      )

      g2.setColor(savedColor)
      g2.setFont(savedFont)
    }
  }
}
