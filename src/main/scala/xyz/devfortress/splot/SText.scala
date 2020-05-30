package xyz.devfortress.splot

import java.awt.geom.AffineTransform
import java.awt.{Color, Font, Graphics2D}

import org.scilab.forge.jlatexmath.{TeXConstants, TeXFormula}

sealed trait SText

case class PlainText(text: String) extends SText {
  def draw(g2: Graphics2D, atPosition: (Int, Int), anchor: Anchor, color: Color, angle: Double, font: Font): Unit = {
    if (text.nonEmpty) {
      g2.drawAndRestore { g2 =>
        g2.withColor(color)
          .withFont(font)
          .withTransform(AffineTransform.getRotateInstance(Math.PI * (-angle / 180), atPosition._1, atPosition._2))
          .draw { _ =>
            val metrics = g2.getFontMetrics
            val xy = atAnchor(atPosition, anchor, metrics.stringWidth(text), metrics.getAscent)
            g2.drawString(text, xy._1, xy._2)
          }
      }
    }
  }

  private def atAnchor(atPosition: (Int, Int), anchor: Anchor, width: Int, height: Int): (Int, Int) = {
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

case class LaTeXText(text: String) extends SText {
   def draw(g2: Graphics2D,
            atPosition: (Int, Int),
            anchor: Anchor,
            color: Color,
            angle: Double,
            fontSize: Int): Unit = {
     if (text.nonEmpty) {
       val formula = new TeXFormula(text)
       val icon = new formula.TeXIconBuilder()
         .setFGColor(color)
         .setStyle(TeXConstants.STYLE_TEXT)
         .setSize(fontSize.toFloat)
         .build()
       val width = icon.getIconWidth
       val height = icon.getIconHeight
       val xy = atAnchor(atPosition, anchor, width, height)
       g2.withTransform(AffineTransform.getRotateInstance(Math.PI * (-angle / 180), atPosition._1, atPosition._2))
         .withColor(Color.BLACK)
         .draw(_ => {
           icon.paintIcon(null, g2, xy._1, xy._2)
         })
     }
  }

  private def atAnchor(atPosition: (Int, Int), anchor: Anchor, width: Int, height: Int): (Int, Int) = {
    anchor match {
      case Anchor.LEFT_LOWER   => (atPosition._1, atPosition._2 - height)
      case Anchor.LEFT_UPPER   => (atPosition._1, atPosition._2)
      case Anchor.RIGHT_LOWER  => (atPosition._1 - width , atPosition._2 - height)
      case Anchor.RIGHT_UPPER  => (atPosition._1 - width , atPosition._2)
      case Anchor.CENTER_LOWER => (atPosition._1 - width / 2 , atPosition._2 - height)
      case Anchor.CENTER_UPPER => (atPosition._1 - width / 2 , atPosition._2)
      case Anchor.CENTER       => (atPosition._1 - width / 2 , atPosition._2 - height / 2)
    }
  }
}
