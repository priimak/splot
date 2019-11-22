package xyz.devfortress.splot

import java.awt.geom.AffineTransform
import java.awt.{Color, Font, Graphics2D, Stroke}

final case class Graphics2DHelper(g2: Graphics2D, var finallyActions: Seq[() => Unit] = Seq()) {

  def drawAndRestore(action: Function[Graphics2D, Unit]): Unit = {
    val savedStroke = g2.getStroke
    val savedFont = g2.getFont
    val savedColor = g2.getColor
    val savedTransform = g2.getTransform
    try {
      action.apply(g2)
    } finally {
      g2.setTransform(savedTransform)
      g2.setColor(savedColor)
      g2.setFont(savedFont)
      g2.setStroke(savedStroke)
    }
  }

  def draw(action: Graphics2D => Unit): Unit = {
    try {
      action.apply(g2)
    } finally {
      finallyActions.reverseIterator.foreach(f => f.apply())
    }
  }

  def withColor(color: Color): Graphics2DHelper = {
    val savedColor = g2.getColor
    g2.setColor(color)
    this.finallyActions = this.finallyActions ++ Seq(() => g2.setColor(savedColor));
    this
  }

  def withStroke(stroke: Stroke): Graphics2DHelper = {
    val savedStroke = g2.getStroke
    g2.setStroke(stroke)
    this.finallyActions = this.finallyActions ++ Seq(() => g2.setStroke(savedStroke))
    this
  }

  def withFont(font: Font): Graphics2DHelper = {
    val savedFont = g2.getFont
    g2.setFont(font)
    this.finallyActions = this.finallyActions ++ Seq(() => g2.setFont(savedFont))
    this
  }

  def withTransform(theta: Double, x: Double, y: Double): Graphics2DHelper = {
    val savedTransform = g2.getTransform
    g2.rotate(theta, x, y)
    this.finallyActions = this.finallyActions ++ Seq(() => g2.setTransform(savedTransform))
    this
  }

  def withTransform(transform: AffineTransform): Graphics2DHelper = {
    val savedTransform = g2.getTransform
    g2.setTransform(transform)
    this.finallyActions = this.finallyActions ++ Seq(() => g2.setTransform(savedTransform))
    this
  }
}
