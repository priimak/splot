package xyz.devfortress.splot

import java.awt.{Color, Font}

object XYLabels {
  val DEFAULT_XLABEL_PLOTTER: (DrawingContext, Int, SText) => Unit = xLabelPlotter()
  val DEFAULT_YLABEL_PLOTTER: (DrawingContext, Int, SText) => Unit = yLabelPlotter()

  def xLabelPlotter[A : STextLike](
        font: Font = Font.decode("Monospaced-15"),
        fontSize: Int = 15,
        color: Color = Color.BLACK)
      (ctx: DrawingContext, ticksHeight: Int, label: A): Unit = {
    import ctx._
    drawLabel(
      atPosition = (
        ((imageWidth - leftPadding - rightPadding)/ 2.0 + leftPadding).toInt,
        (imageHeight - (bottomPadding - ticksHeight) / 2.0).toInt
      ),
      angle = 0, font = font, fontSize = fontSize, color = color,
      ctx = ctx, label = label
    )
  }

  def yLabelPlotter[A : STextLike](
      font: Font = Font.decode("Monospaced-15"),
      fontSize: Int = 15,
      color: Color = Color.BLACK)
    (ctx: DrawingContext, ticksWidth: Int, label: A): Unit = {
    import ctx._
    drawLabel(
      atPosition = (
        ((leftPadding - ticksWidth)/ 2.0).toInt,
        ((imageHeight - topPadding - bottomPadding) / 2.0 + topPadding).toInt
      ),
      angle = 90, font = font, fontSize = fontSize, color = color,
      ctx = ctx, label = label
    )
  }

  private def drawLabel[A : STextLike](
                         atPosition: (Int, Int),
                         angle: Int,
                         font: Font,
                         fontSize: Int,
                         color: Color,
                         ctx: DrawingContext,
                         label: A
                       ): Unit = {
    import ctx._
    val sText = implicitly[STextLike[A]].asSText(label)
    sText match {
      case ptext: PlainText =>
        ptext.draw(g2, atPosition, Anchor.CENTER, color, angle = angle, font.deriveFont(fontSize.toFloat))
      case latex: LaTeXText =>
        latex.draw(g2, atPosition, Anchor.CENTER, color, angle = angle, fontSize)
    }
  }
}
