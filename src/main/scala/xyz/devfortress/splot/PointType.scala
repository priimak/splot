package xyz.devfortress.splot

import java.awt.{Color, Graphics2D}

sealed trait PointTypeLike[T] {
  def asPointType(v: T): PointType
}

object PointTypeLike {
  def apply[T:PointTypeLike]: PointTypeLike[T] = implicitly

  implicit val pts: PointTypeLike[String] = new PointTypeLike[String] {
    override def asPointType(v: String): PointType = v match {
      case "." => PointType.Dot
      case "+" => PointType.Cross
      case "x" => PointType.X
      case "o" => PointType.Circle
      case "s" => PointType.Square
      case "d" => PointType.Diamond
      case _ => throw new IllegalArgumentException(s"Unable to derive point type from '$v'")
    }
  }

  implicit val ptp: PointTypeLike[PointType] = new PointTypeLike[PointType] {
    override def asPointType(v: PointType): PointType = v
  }
}

sealed abstract class PointType extends CommonSPlotLibTrait {
  def drawAt(point: (Int, Int), size: Int, halfSize: Int, fillColor: Option[Color], g2: Graphics2D): Unit
}

object PointType {
  private def withColor(g2: Graphics2D, color: Option[Color])(action: => Unit): Unit = {
    color match {
      case Some(c) =>
        val savedColor = g2.getColor
        try {
          g2.setColor(c)
          action
        } finally {
          g2.setColor(savedColor)
        }
      case None => // NOOP
    }
  }

  val Dot: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, fillColor: Option[Color], g2: Graphics2D): Unit =
      withColor(g2, fillColor) {
        g2.fillOval(point._1 - halfSize, point._2 - halfSize, size, size)
      }
  }

  val Cross: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, fillColor: Option[Color], g2: Graphics2D): Unit = {
      g2.drawLine(point._1 - halfSize, point._2, point._1 + halfSize, point._2)
      g2.drawLine(point._1, point._2 - halfSize, point._1, point._2 + halfSize)
    }
  }

  val X: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, fillColor: Option[Color], g2: Graphics2D): Unit = {
      g2.drawLine(point._1 - halfSize, point._2 - halfSize, point._1 + halfSize, point._2 + halfSize)
      g2.drawLine(point._1 - halfSize, point._2 + halfSize, point._1 + halfSize, point._2 - halfSize)
    }
  }

  val Square: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, fillColor: Option[Color], g2: Graphics2D): Unit = {
      withColor(g2, fillColor) {
        g2.fillRect(point._1 - halfSize, point._2 - halfSize, size, size)
      }
      g2.drawRect(point._1 - halfSize, point._2 - halfSize, size, size)
    }
  }

  val Diamond: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, fillColor: Option[Color], g2: Graphics2D): Unit = {
      withColor(g2, fillColor) {
        g2.fillPolygon(
          Array[Int](point._1 - halfSize, point._1, point._1 + halfSize, point._1),
          Array[Int](point._2, point._2 + halfSize, point._2, point._2 - halfSize),
          4
        )
      }
      g2.drawPolygon(
        Array[Int](point._1 - halfSize, point._1, point._1 + halfSize, point._1),
        Array[Int](point._2, point._2 + halfSize, point._2, point._2 - halfSize),
        4
      )
    }
  }

  val Circle: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, fillColor: Option[Color], g2: Graphics2D): Unit = {
      withColor(g2, fillColor) {
        g2.fillOval(point._1 - halfSize, point._2 - halfSize, size, size)
      }
      g2.drawOval(point._1 - halfSize, point._2 - halfSize, size, size)
    }
  }
}