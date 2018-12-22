package xyz.devfortress.splot

import java.awt.Color

/**
 * Type class for all things that can be converted into java.awt.Color. Specifically this is used to convert color
 * names (strings) into Color.
 */
sealed trait ColorLike[T] {
  def asColor(v: T): Color
}

object ColorLike {
  def apply[T: ColorLike]: ColorLike[T] = implicitly

  val colorMap: Map[String, Color] = Map(
    "red" -> Color.RED,
    "green" -> Color.GREEN,
    "blue" -> Color.BLUE,
    "black" -> Color.BLACK,
    "cyan" -> Color.CYAN,
    "gray" -> Color.GRAY,
    "lightgray" -> Color.LIGHT_GRAY,
    "magenta" -> Color.MAGENTA,
    "orange" -> Color.ORANGE,
    "pink" -> Color.PINK,
    "white" -> Color.WHITE,
    "yellow" -> Color.YELLOW
  )

  implicit val stringColorLike: ColorLike[String] = new ColorLike[String] {
    override def asColor(str: String): Color = colorMap.get(str) match {
      case Some(color) => color
      case None => throw new IllegalArgumentException(
        s"Supported colors are ${colorMap.keys.map("\"" + _ + "\"").mkString(" ")}."
      )
    }
  }

  implicit val colorColorLike: ColorLike[Color] = new ColorLike[Color] {
    override def asColor(color: Color): Color = color
  }
}
