package xyz.devfortress.splot

trait SeqOfDoubleTuples[T] {
  def asDoubleSeq(v: Seq[T]): Seq[(Double, Double)]
}

object SeqOfDoubleTuples {
  def apply[T: SeqOfDoubleTuples]: SeqOfDoubleTuples[T] = implicitly

  implicit val intint: SeqOfDoubleTuples[(Int, Int)] = v => v.map(xy => (xy._1.toDouble, xy._2.toDouble))
  implicit val intlong: SeqOfDoubleTuples[(Int, Long)] = v => v.map(xy => (xy._1.toDouble, xy._2.toDouble))
  implicit val intdouble: SeqOfDoubleTuples[(Int, Double)] = v => v.map(xy => (xy._1.toDouble, xy._2))
  implicit val longlong: SeqOfDoubleTuples[(Long, Long)] = v => v.map(xy => (xy._1.toDouble, xy._2.toDouble))
  implicit val longint: SeqOfDoubleTuples[(Long, Int)] = v => v.map(xy => (xy._1.toDouble, xy._2.toDouble))
  implicit val longdouble: SeqOfDoubleTuples[(Long, Double)] = v => v.map(xy => (xy._1.toDouble, xy._2))
  implicit val doubleint: SeqOfDoubleTuples[(Double, Int)] = v => v.map(xy => (xy._1, xy._2.toDouble))
  implicit val doublelong: SeqOfDoubleTuples[(Double, Long)] = v => v.map(xy => (xy._1, xy._2.toDouble))
  implicit val doubledouble: SeqOfDoubleTuples[(Double, Double)] = v => v
}