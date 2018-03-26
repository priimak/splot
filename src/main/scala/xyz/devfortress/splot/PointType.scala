package xyz.devfortress.splot

import java.awt.Graphics2D

sealed trait PointTypeLike[T] {
  def asPointType(v: T): PointType
}

object PointTypeLike {
  def apply[T:PointTypeLike]: PointTypeLike[T] = implicitly

  implicit val pts: PointTypeLike[String] = new PointTypeLike[String] {
    override def asPointType(v: String): PointType = v match {
      case "." => PointType.Dot
      case "+" => PointType.Cross
      case "o" => PointType.Circle
      case _ => throw new IllegalArgumentException("Supported point types are \".\", \"+\" and \"o\".")
    }
  }

  implicit val ptp: PointTypeLike[PointType] = new PointTypeLike[PointType] {
    override def asPointType(v: PointType): PointType = v
  }
}

sealed abstract class PointType extends CommonSPlotLibTrait {
  def drawAt(point: (Int, Int), size: Int, halfSize: Int, g2: Graphics2D): Unit
}

object PointType {
  val Dot: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, g2: Graphics2D): Unit =
      g2.fillRect(point._1 - halfSize, point._2 - halfSize, size, size)
  }

  val Cross: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, g2: Graphics2D): Unit = {
      g2.drawLine(point._1 - halfSize, point._2, point._1 + halfSize, point._2)
      g2.drawLine(point._1, point._2 - halfSize, point._1, point._2 + halfSize)
    }
  }

  val Circle: PointType = new PointType {
    override def drawAt(point: (Int, Int), size: Int, halfSize: Int, g2: Graphics2D): Unit =
      g2.fillOval(point._1 - halfSize, point._2 - halfSize, size, size)
  }
}