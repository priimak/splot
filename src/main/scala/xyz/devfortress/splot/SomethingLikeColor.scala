package xyz.devfortress.splot

import java.awt.Color

trait SomethingLikeColor[T] {
  def asSomething(v: T): Option[Color]
}

object SomethingLikeColor {
  def apply[T:SomethingLikeColor]: SomethingLikeColor[T] = implicitly

  implicit val c2c: SomethingLikeColor[Color] = (color: Color) => Some(color)
  implicit val s2c: SomethingLikeColor[String] = (color: String) => Some(ColorLike.s2c.asColor(color))
  implicit val o2c: SomethingLikeColor[Some[Color]] = (v: Some[Color]) => v
  implicit val n2c: SomethingLikeColor[Option[Nothing]] = (v: Option[Nothing]) => None
}
