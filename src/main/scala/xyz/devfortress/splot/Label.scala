package xyz.devfortress.splot

import java.awt.{Color, Font, Graphics2D}

/**
 * Label to be drawn at the position (x,y) on the plot. Label is anchored at lower left corner of the bounding
 * box of the text.
 *
 * @param text text of the label to draw.
 * @param x x-coordinate at which to draw label.
 * @param y y-coordinate at which to draw label.
 * @param font font to use for this label. Default font it "Sans-Serif".
 * @param color color of text. Default is black.
 * @param anchor where text is to be anchored.
 */
sealed case class Label(text: String, x: Double, y: Double, font: Font = Font.getFont(Font.SANS_SERIF),
      color: Color = Color.BLACK, anchor: Anchor = Anchor.LEFT_LOWER) {
  def draw(g2: Graphics2D, atPosition: (Double, Double)): Unit = {
    val savedFont = g2.getFont
    val savedColor = g2.getColor
    g2.setFont(font)
    g2.setColor(color)

    val metrics = g2.getFontMetrics
    val width = metrics.stringWidth(text)
    val xy = anchor match {
      case Anchor.LEFT_LOWER => atPosition
      case Anchor.LEFT_UPPER => (atPosition._1, atPosition._2 + metrics.getAscent)
      case Anchor.RIGHT_LOWER => (atPosition._1 - width , atPosition._2)
      case Anchor.RIGHT_UPPER => (atPosition._1 - width , atPosition._2 + metrics.getAscent)
      case Anchor.CENTER_LOWER => (atPosition._1 - width / 2 , atPosition._2)
      case Anchor.CENTER_UPPER => (atPosition._1 - width / 2 , atPosition._2 + metrics.getAscent)
      case Anchor.CENTER => (atPosition._1 - width / 2 , atPosition._2 + metrics.getAscent / 2)
    }
    g2.drawString(text, xy._1.toInt, xy._2.toInt)

    g2.setFont(savedFont)
    g2.setColor(savedColor)
  }
}
