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
 */
sealed case class Label(text: String, x: Double, y: Double, font: Font = Font.getFont(Font.SANS_SERIF),
      color: Color = Color.BLACK) {
  def draw(g2: Graphics2D, atPosition: (Double, Double)): Unit = {
    val savedFont = g2.getFont
    val savedColor = g2.getColor
    g2.setFont(font)
    g2.setColor(color)
    g2.drawString(text, atPosition._1.toInt, atPosition._2.toInt)
    g2.setFont(savedFont)
    g2.setColor(savedColor)
  }
}
