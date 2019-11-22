package xyz.devfortress.splot

import java.awt.geom.AffineTransform
import java.awt.{Color, Font, Graphics2D}

import org.scilab.forge.jlatexmath.{TeXConstants, TeXFormula}

sealed trait SText {
  def draw(g2: Graphics2D, atPosition: (Int, Int),
           anchor: Anchor, color: Color = Color.BLACK, angle: Double = 0.0): Unit;

  protected def atAnchor(atPosition: (Int, Int), anchor: Anchor, width: Int, height: Int): (Int, Int) = {
    anchor match {
      case Anchor.LEFT_LOWER   => atPosition
      case Anchor.LEFT_UPPER   => (atPosition._1, atPosition._2 + height)
      case Anchor.RIGHT_LOWER  => (atPosition._1 - width , atPosition._2)
      case Anchor.RIGHT_UPPER  => (atPosition._1 - width , atPosition._2 + height)
      case Anchor.CENTER_LOWER => (atPosition._1 - width / 2 , atPosition._2)
      case Anchor.CENTER_UPPER => (atPosition._1 - width / 2 , atPosition._2 + height)
      case Anchor.CENTER       => (atPosition._1 - width / 2 , atPosition._2 + height / 2)
    }
  }
}

case class PlainText(text: String, font: Font = Figure.DEFAULT_FONT) extends SText {
  override def draw(g2: Graphics2D, atPosition: (Int, Int), anchor: Anchor, color: Color, angle: Double): Unit = {
    g2.drawAndRestore { g2 =>
      g2.setFont(if (angle == 0.0) font else font.deriveFont(AffineTransform.getRotateInstance(angle)))
      g2.setColor(color)

      val metrics = g2.getFontMetrics
      val xy = atAnchor(atPosition, anchor, metrics.stringWidth(text), metrics.getAscent)
      g2.drawString(text, xy._1, xy._2)
    }
  }
}

case class LaTeXText(text: String, fontSize: Int = 20) extends SText {
  override def draw(g2: Graphics2D, atPosition: (Int, Int), anchor: Anchor, color: Color, angle: Double): Unit = {
    if (angle != 0.0) g2.setTransform(AffineTransform.getRotateInstance(angle))
    val formula = new TeXFormula(text)
    val icon = new formula.TeXIconBuilder()
      .setFGColor(color)
      .setStyle(TeXConstants.STYLE_TEXT)
      .setSize(fontSize)
      .build()
    val width = icon.getIconWidth
    val height = icon.getIconHeight
    val xy = atAnchor(atPosition, anchor, width, height)
    icon.paintIcon(null, g2, xy._1, xy._2)
  }
}
