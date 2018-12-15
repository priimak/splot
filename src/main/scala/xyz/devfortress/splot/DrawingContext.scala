package xyz.devfortress.splot

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap

/**
 * Class passed to Plot.draw(...) function for each plot to draw itself.
 *
 * @param g2 Graphics2D object used for drawing.
 * @param x2i Function that translated from in-plot x-coordinate into image x-position of related pixel.
 * @param y2i Function that translated from in-plot y-coordinate into image y-position of related pixel.
 * @param imageWidth Total image width in pixels that includes padding and plot area.
 * @param imageHeight Total image height in pixels that includes padding and plot area.
 * @param rightPadding Distance in pixels from the right edge of the plot area to the right edge of the figure/image.
 * @param leftPadding Distance in pixels from the right edge of the figure/image to the beginning of the plot area.
 * @param topPadding Distance in pixels from the top edge of the figure/image to the top edge of the plot area.
 * @param bottomPadding Distance in pixels from the bottom edge of the plot area to the bottom of the figure/image.
 * @param currentDomain Domain of the in-plot x-coordinate values.
 * @param currentRange Range of the in-plot x-coordinate values.
 * @param zRanges
 * @param image BufferedImage on which drawing can take place.
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
