package xyz.devfortress.splot

sealed case class LineType(descr: String, dashPattern: Seq[Float]) {
  override def toString: String = descr
}

object LineType {
  object SOLID extends LineType("LineType.SOLID", Seq[Float]())
  object DASHES extends LineType("LineType.DASHES", Seq[Float](5, 5))
  object DASHES_AND_DOTS extends LineType("LineType.DASHES_AND_DOTS", Seq[Float](5, 5, 1))
  object DOTS extends LineType("LineType.DOTS", Seq[Float](1, 5))
}

sealed trait LinesTypeLike[-T] {
  def asLineType(v: T): LineType
}

object LinesTypeLike {
  def apply[T: LinesTypeLike]: LinesTypeLike[T] = implicitly

  implicit val stringLineTypeLike: LinesTypeLike[String] = new LinesTypeLike[String] {
    override def asLineType(v: String): LineType = v match {
      case "-" => LineType.SOLID
      case "--" => LineType.DASHES
      case "-." => LineType.DASHES_AND_DOTS
      case "." => LineType.DOTS
      case _ => throw new IllegalArgumentException(s"Unable to translate string '$v' into a line pattern")
    }
  }

  implicit val lineTypeLineTypeLike: LinesTypeLike[LineType] = new LinesTypeLike[LineType] {
    override def asLineType(v: LineType): LineType = v
  }
}