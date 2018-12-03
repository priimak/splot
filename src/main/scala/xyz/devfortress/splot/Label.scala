package xyz.devfortress.splot

import java.awt.{Font, Graphics2D}

/**
 * Label to be drawn at the position (x,y) on the plot. Test of label is anchored at lower left corner of the bounding
 * box of the text.
 *
 * @param label text of the lable to draw.
 * @param x x-coordinate at which to draw label.
 * @param y y-coordinate at which to draw label.
 * @param font font to use for this label. Default font it "Sans-Serif".
 */
sealed case class Label(label: String, x: Double, y: Double, font: Font = Font.getFont(Font.SANS_SERIF)) {
  def draw(g2: Graphics2D, atPosition: (Double, Double)): Unit = {
    val savedFont = g2.getFont
    g2.setFont(font)
    g2.drawString(label, atPosition._1.toInt, atPosition._2.toInt)
    g2.setFont(savedFont)
  }
}
