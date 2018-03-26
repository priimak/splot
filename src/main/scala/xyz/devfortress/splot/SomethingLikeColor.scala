package xyz.devfortress.splot

import java.awt.Color

trait SomethingLikeColor[T] {
  def asSomething(v: T): Option[Color]
}

object SomethingLikeColor {
  def apply[T:SomethingLikeColor]: SomethingLikeColor[T] = implicitly

  implicit val c2c: SomethingLikeColor[Color] = new SomethingLikeColor[Color] {
    override def asSomething(color: Color): Option[Color] = Some(color)
  }

  implicit val s2c: SomethingLikeColor[String] = new SomethingLikeColor[String] {
    override def asSomething(color: String): Option[Color] = Some(ColorLike.s2c.asColor(color))
  }

  implicit val o2c: SomethingLikeColor[Some[Color]] = new SomethingLikeColor[Some[Color]] {
    override def asSomething(v: Some[Color]): Some[Color] = v
  }

  implicit val n2c: SomethingLikeColor[Option[Nothing]] = new SomethingLikeColor[Option[Nothing]] {
    override def asSomething(v: Option[Nothing]): Option[Color] = None
  }
}
