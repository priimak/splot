package xyz.devfortress.splot

import java.awt.image.BufferedImage
import java.nio.file.Paths

import javax.imageio.ImageIO

class SPlotImage (val image: BufferedImage) {
  def save(fileName: String): Unit = {
    val saveToFilePath = Paths.get(fileName)
    if (saveToFilePath.toString.endsWith(".png")) {
      ImageIO.write(image, "png", saveToFilePath.toFile)
    } else if(saveToFilePath.toString.endsWith(".jpg")) {
      ImageIO.write(image, "jpg", saveToFilePath.toFile)
    } else {
      throw new IllegalArgumentException(s"Unrecognized image file type in $fileName")
    }
  }
}
