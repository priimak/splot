package xyz.devfortress.splot

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap

/**
 * Class passed to Plot.draw(...) function for each plot to draw itself.
 */
final case class DrawingContext(
  g2: Graphics2D,
  x2i: Double => Int,
  y2i: Double => Int,
  imageWidth: Int,
  imageHeight: Int,
  rightPadding: Int,
  leftPadding: Int,
  topPadding: Int,
  bottomPadding: Int,
  currentDomain: (Double, Double),
  currentRange: (Double, Double),
  zRanges: ConcurrentHashMap[Int, (Double, Double)],
  image: BufferedImage
)
