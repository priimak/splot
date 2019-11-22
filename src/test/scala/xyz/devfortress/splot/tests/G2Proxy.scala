package xyz.devfortress.splot.tests

import java.awt._
import java.awt.font.{FontRenderContext, GlyphVector}
import java.awt.geom.AffineTransform
import java.awt.image.renderable.RenderableImage
import java.awt.image.{BufferedImage, BufferedImageOp, ImageObserver, RenderedImage}
import java.text.AttributedCharacterIterator
import java.util

import scala.collection.{JavaConverters, mutable}

sealed trait ProxyAction
object Capture extends ProxyAction
case class Verify(calls: util.Iterator[CapturedCall]) extends ProxyAction

case class CapturedCall(name: String, arguments: Seq[Any], retval: Any) {
  def this(args: Seq[Any], retval: Any) {
    this(Thread.currentThread().getStackTrace()(3).getMethodName, args, retval)
  }

  override def toString: String = s"$name(${CapturedCall.o2s(arguments)})"
}

object CapturedCall {
  def o2s(as: Seq[Any]): String = {

    as.map(Option.apply).map {
      case None => "null"
      case Some(e) => e match {
        case xs: Some[Array[_]] => xs.map(_.toString).mkString("[", ",", "]")
        case bs: BasicStroke => s"BasicStroke(${bs.getLineWidth}, ${bs.getEndCap}, " +
          s"${bs.getLineJoin}, ${bs.getMiterLimit}, ${bs.getDashArray}, ${bs.getDashPhase})"
        case a => a.toString
      }
    }.mkString("[", ",", "]")
  }
}

class G2Proxy(val g2: Graphics2D, proxyAction: ProxyAction = Capture) extends Graphics2D {

  private def this(g2Proxy: G2Proxy) {
    this(g2Proxy.g2, Verify(g2Proxy.capture.iterator))
  }

  private val capture = new util.ArrayList[CapturedCall]()

  def getCapture(): Seq[CapturedCall] = JavaConverters.asScalaIteratorConverter(capture.iterator).asScala.toSeq

  def verify()(checks: G2Proxy => Unit): Unit = checks(new G2Proxy(this))

  private def proxyActionDo[T](arguments: Seq[Any], action : => T): T = {
    proxyAction match {
      case Capture =>
        val retval = action
        capture.add(new CapturedCall(arguments, retval))
        retval
      case Verify(calls) => {
        if (calls.hasNext) {
          val call: CapturedCall = calls.next()
          val callingMethodName = Thread.currentThread().getStackTrace()(2).getMethodName
          assert(call.name == callingMethodName,
            s"Instead of $callingMethodName(...) method ${call.name}(...) was called")
          val argsForCompare = arguments.map {
            case a: Array[_] => a.toSeq
            case a => a
          }
          val callArgsForCompare = call.arguments.map {
            case a: Array[_] => a.toSeq
            case a => a
          }
          assert(
            callArgsForCompare == argsForCompare,
            s"Not matching arguments for method ${call.name}(...). " +
              s"Was expecting ${CapturedCall.o2s(arguments)} but got ${CapturedCall.o2s(call.arguments)}"
          )
          call.retval.asInstanceOf[T]
        } else {
          throw new AssertionError("No more calls where made on this object")
        }
      }
    }
  }

  override def draw(s: Shape): Unit = {
    proxyActionDo(Seq(s), g2.draw(s))
  }

  override def drawImage(img: Image, xform: AffineTransform, obs: ImageObserver): Boolean = {
    proxyActionDo(Seq(img, xform, obs), g2.drawImage(img, xform, obs))
  }

  override def drawImage(img: BufferedImage, op: BufferedImageOp, x: Int, y: Int): Unit = {
    proxyActionDo(Seq(img, op, x, y), g2.drawImage(img, op, x, y))
  }

  override def drawRenderedImage(img: RenderedImage, xform: AffineTransform): Unit = {
    proxyActionDo(Seq(img, xform), g2.drawRenderedImage(img, xform))
  }
  override def drawRenderableImage(img: RenderableImage, xform: AffineTransform): Unit = {
    proxyActionDo(Seq(img, xform), g2.drawRenderableImage(img, xform))
  }

  override def drawString(str: String, x: Int, y: Int): Unit = {
    proxyActionDo(Seq(str, x, y), g2.drawString(str, x, y))
  }

  override def drawString(str: String, x: Float, y: Float): Unit = {
    proxyActionDo(Seq(str, x, y), g2.drawString(str, x, y))
  }

  override def drawString(iterator: AttributedCharacterIterator, x: Int, y: Int): Unit = {
    proxyActionDo(Seq(iterator, x, y), g2.drawString(iterator, x, y))
  }

  override def drawString(iterator: AttributedCharacterIterator, x: Float, y: Float): Unit = {
    proxyActionDo(Seq(iterator, x, y), g2.drawString(iterator, x, y))
  }

  override def drawGlyphVector(g: GlyphVector, x: Float, y: Float): Unit = {
    proxyActionDo(Seq(g, x, y), g2.drawGlyphVector(g, x, y))
  }

  override def fill(s: Shape): Unit = {
    proxyActionDo(Seq(s), g2.fill(s))
  }

  override def hit(rect: Rectangle, s: Shape, onStroke: Boolean): Boolean = {
    proxyActionDo(Seq(rect, s, onStroke), g2.hit(rect, s, onStroke))
  }

  override def getDeviceConfiguration: GraphicsConfiguration = {
    proxyActionDo(Seq(), g2.getDeviceConfiguration)
  }

  override def setComposite(comp: Composite): Unit = {
    proxyActionDo(Seq(comp), g2.setComposite(comp))
  }

  override def setPaint(paint: Paint): Unit = {
    proxyActionDo(Seq(paint), g2.setPaint(paint))
  }

  override def setStroke(s: Stroke): Unit = {
    proxyActionDo(Seq(s), g2.setStroke(s))
  }

  override def setRenderingHint(hintKey: RenderingHints.Key, hintValue: Any): Unit = {
    proxyActionDo(Seq(hintKey, hintValue), g2.setRenderingHint(hintKey, hintValue))
  }

  override def getRenderingHint(hintKey: RenderingHints.Key): AnyRef = {
    proxyActionDo(Seq(hintKey), g2.getRenderingHint(hintKey))
  }

  override def setRenderingHints(hints: util.Map[_, _]): Unit = {
    proxyActionDo(Seq(hints), g2.setRenderingHints(hints))
  }

  override def addRenderingHints(hints: util.Map[_, _]): Unit = {
    proxyActionDo(Seq(hints), g2.addRenderingHints(hints))
  }

  override def getRenderingHints: RenderingHints = {
    proxyActionDo(Seq(), g2.getRenderingHints)
  }

  override def translate(x: Int, y: Int): Unit = {
    proxyActionDo(Seq(x, y), g2.translate(x, y))
  }

  override def translate(tx: Double, ty: Double): Unit = {
    proxyActionDo(Seq(tx, ty), g2.translate(tx, ty))
  }

  override def rotate(theta: Double): Unit = {
    proxyActionDo(Seq(theta), g2.rotate(theta))
  }

  override def rotate(theta: Double, x: Double, y: Double): Unit = {
    proxyActionDo(Seq(theta, x, y), g2.rotate(theta, x, y))
  }

  override def scale(sx: Double, sy: Double): Unit = {
    proxyActionDo(Seq(sx, sy), g2.scale(sx, sy))
  }

  override def shear(shx: Double, shy: Double): Unit = {
    proxyActionDo(Seq(shx, shy), g2.shear(shx, shy))
  }

  override def transform(tx: AffineTransform): Unit = {
    proxyActionDo(Seq(tx), g2.transform(tx))
  }

  override def setTransform(tx: AffineTransform): Unit = {
    proxyActionDo(Seq(tx), g2.setTransform(tx))
  }

  override def getTransform: AffineTransform = {
    proxyActionDo(Seq(), g2.getTransform)
  }

  override def getPaint: Paint = {
    proxyActionDo(Seq(), g2.getPaint)
  }

  override def getComposite: Composite = {
    proxyActionDo(Seq(), g2.getComposite)
  }

  override def setBackground(color: Color): Unit = {
    proxyActionDo(Seq(color), g2.setBackground(color))
  }

  override def getBackground: Color = {
    proxyActionDo(Seq(), g2.getBackground)
  }

  override def getStroke: Stroke = {
    proxyActionDo(Seq(), g2.getStroke)
  }

  override def clip(s: Shape): Unit = {
    proxyActionDo(Seq(s), g2.clip(s))
  }

  override def getFontRenderContext: FontRenderContext = {
    proxyActionDo(Seq(), g2.getFontRenderContext)
  }

  override def create(): Graphics = {
    proxyActionDo(Seq(), g2.create())
  }

  override def getColor: Color = {
    proxyActionDo(Seq(), g2.getColor)
  }

  override def setColor(c: Color): Unit = {
    proxyActionDo(Seq(c), g2.setColor(c))
  }

  override def setPaintMode(): Unit = {
    proxyActionDo(Seq(), g2.setPaintMode())
  }

  override def setXORMode(c1: Color): Unit = {
    proxyActionDo(Seq(c1), g2.setXORMode(c1))
  }

  override def getFont: Font = {
    proxyActionDo(Seq(), g2.getFont)
  }

  override def setFont(font: Font): Unit = {
    proxyActionDo(Seq(font), g2.setFont(font))
  }

  override def getFontMetrics(f: Font): FontMetrics = {
    proxyActionDo(Seq(f), g2.getFontMetrics(f))
  }

  override def getClipBounds: Rectangle = {
    proxyActionDo(Seq(), g2.getClipBounds())
  }

  override def clipRect(x: Int, y: Int, width: Int, height: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height), g2.clearRect(x, y, width, height))
  }

  override def setClip(x: Int, y: Int, width: Int, height: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height), g2.setClip(x, y, width, height))
  }

  override def getClip: Shape = {
    proxyActionDo(Seq(), g2.getClip)
  }

  override def setClip(clip: Shape): Unit = {
    proxyActionDo(Seq(clip), g2.setClip(clip))
  }

  override def copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height, dx, dy), g2.copyArea(x, y, width, height, dx, dy))
  }

  override def drawLine(x1: Int, y1: Int, x2: Int, y2: Int): Unit = {
    proxyActionDo(Seq(x1, y1, x2, y2), g2.drawLine(x1, y1, x2, y2))
  }

  override def fillRect(x: Int, y: Int, width: Int, height: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height), g2.fillRect(x, y, width, height))
  }

  override def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height), g2.clearRect(x, y, width, height))
  }

  override def drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int): Unit = {
    proxyActionDo(
      Seq(x, y, width, height, arcWidth, arcHeight), g2.drawRoundRect(x, y, width, height, arcWidth, arcHeight)
    )
  }

  override def fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int): Unit = {
    proxyActionDo(
      Seq(x, y, width, height, arcWidth, arcHeight), g2.fillRoundRect(x, y, width, height, arcWidth, arcHeight)
    )
  }

  override def drawOval(x: Int, y: Int, width: Int, height: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height), g2.drawOval(x, y, width, height))
  }

  override def fillOval(x: Int, y: Int, width: Int, height: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height), g2.fillOval(x, y, width, height))
  }

  override def drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height, startAngle, arcAngle), g2.drawArc(x, y, width, height, startAngle, arcAngle))
  }

  override def fillArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int): Unit = {
    proxyActionDo(Seq(x, y, width, height, startAngle, arcAngle), g2.fillArc(x, y, width, height, startAngle, arcAngle))
  }

  override def drawPolyline(xPoints: Array[Int], yPoints: Array[Int], nPoints: Int): Unit = {
    proxyActionDo(Seq(xPoints, yPoints, nPoints), g2.drawPolyline(xPoints, yPoints, nPoints))
  }

  override def drawPolygon(xPoints: Array[Int], yPoints: Array[Int], nPoints: Int): Unit =
    proxyActionDo(Seq(xPoints, yPoints, nPoints), g2.drawPolygon(xPoints, yPoints, nPoints))

  override def fillPolygon(xPoints: Array[Int], yPoints: Array[Int], nPoints: Int): Unit =
    proxyActionDo(Seq(xPoints, yPoints, nPoints), g2.fillPolygon(xPoints, yPoints, nPoints))

  override def drawImage(img: Image, x: Int, y: Int, observer: ImageObserver): Boolean =
    proxyActionDo(Seq(img, x, y, observer), g2.drawImage(img, x, y, observer))

  override def drawImage(img: Image, x: Int, y: Int, width: Int, height: Int, observer: ImageObserver): Boolean =
    proxyActionDo(Seq(img, x, y, width, height, observer), g2.drawImage(img, x, y, width, height, observer))

  override def drawImage(img: Image, x: Int, y: Int, bgcolor: Color, observer: ImageObserver): Boolean =
    proxyActionDo(Seq(img, x, y, bgcolor, observer), g2.drawImage(img, x, y, bgcolor, observer))

  override def drawImage(img: Image, x: Int, y: Int, width: Int, height: Int, bgcolor: Color,
      observer: ImageObserver): Boolean =
    proxyActionDo(
      Seq(img, x, y, width, height, bgcolor, observer), g2.drawImage(img, x, y, width, height, bgcolor, observer)
    )

  override def drawImage(img: Image, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int,
      observer: ImageObserver): Boolean =
    proxyActionDo(
      Seq(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer),
      g2.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
    )

  override def drawImage(img: Image, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int,
      bgcolor: Color, observer: ImageObserver): Boolean =
    proxyActionDo(
      Seq(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer),
      g2.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer)
    )

  override def dispose(): Unit = proxyActionDo(Seq(), g2.dispose())

  override def drawRect(x: Int, y: Int, width: Int, height: Int): Unit =
    proxyActionDo(Seq(x, y, width, height), g2.drawRect(x, y, width, height))

  override def getFontMetrics: FontMetrics = proxyActionDo(Seq(), g2.getFontMetrics)
}

