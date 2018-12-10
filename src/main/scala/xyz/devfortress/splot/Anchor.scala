package xyz.devfortress.splot

sealed trait Anchor

object Anchor {
  object LEFT_LOWER extends Anchor
  object LEFT_UPPER extends Anchor
  object RIGHT_LOWER extends Anchor
  object RIGHT_UPPER extends Anchor
  object CENTER_LOWER extends Anchor
  object CENTER_UPPER extends Anchor
  object CENTER extends Anchor
}
