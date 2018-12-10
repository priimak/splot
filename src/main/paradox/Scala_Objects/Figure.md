# Figure

Figure is primary object in SPlot. Users creates Figure object, add plot elements to it and then request it to be shown
on the screen or save it as image on the disk.

## Constructor
Following parameters (shown with default values) can be passed to the `Figure` constructor:

```bash
name: String = "Figure"
```
Name of the figure that will appear in the name of window. Name will not be shown on the ​plot itself.

```bash
bgColor: Color = Color.WHITE
```
Background color for the whole plot.

```bash
leftPadding : Int = 50
```
Distance in pixes from the left edge of the figure/window to the left bound of the actual plot.

```bash
rightPadding : Int = 50
```
Distance in pixels from the right edge of the figure/window to right bound of the actual plot.

```bash
topPadding : Int = 50
```
Distance in pixels from the top edge of the figure/window to the top bound of the actual plot.

```bash
bottomPadding: Int = 50
```
Distance in pixels from the bottom edge of the figure/window to the top bound of the actual plot.

```bash
domain : Option[(Double, Double)] = None
```
Domain of the plot, i.e. value of `x` in the plot will be drawn from `domain._1` to `domain._2` inclusive.
Due to enabled implicit conversions you can also pass tuples `(Double, Double)` directly. Default value `None`
indicates that domain will be derived from the added plot elements.

```bash
range : Option[(Double, Double)] = None
```
Range of the plot, i.e. value of `y` in the plot will be drawn from `range._1` to `range._2` inclusive. Like for
`domain` you can also pass tuples `(Double, Double)` directly. ​Default value `None` indicates that range will be
derived from the added plot elements.

```bash
xTicks : (Double, Double) => Seq[(Double, String)] = Ticks.ticks10
```
Function that defines generation of ticks on the x-axis. It receives current domain of the plot and returns sequence
of tuples where `_1` refers to position on the x-axis where tick will be drawn and `_2` a label for this tick.
Default value `Ticks.ticks10` is predefined function that tries to crete approximately 10 ticks and the most
appropriate locations.

```bash
yTicks : (Double, Double) => Seq[(Double, String)]
```
Same as `xTicks` but on the y-axis.

```bash
title : String = ""
```
Text to be drawn right above the plot.

```bash
titleFont: Font = Font.decode("Times-20")
```
Font to be used for the title.

```bash
antialiasing: Boolean = true
```
Flag indicating if antialiasing is to be used when drawing plot elements. For plots ​with large number of points or
elements you can obtain noticeable speed up by setting it to `false`.

# Methods

```scala
add(plot: Plot): Unit
add(label: Label): Unit
```
Basic methods for adding plots or labels to the figure. There exists more convenient alternatives for doing the same.
They are listed below.

```scala
show(size: (Int, Int) = (1422, 800)): Figure
```
Build and show window with all plots and labels added to this figure. Parameter `size` is used to define size of the
window frame that is to be created. This method returns the same figure for chaining of calls.

```scala
save(fileName: String): Figure
```
Save figure into an image file. Currently supported formats are `png` and `jpg`. Currently this method can only be
called after `show(...)` was already called and window is showing. This method returns the same figure for chaining of calls.

```scala
close(): Unit
```
Close window frame created previously by calling `show(...)`.

# Convinience methods
<a name="plot"></a>
```bash
plot(data: Seq[D], color: C = Color.BLUE, lw: Int = 1): Unit
[C: ColorLike, D: SeqOfDoubleTuples]
```

<a name="scatter"></a>
```bash
scatter(data: Seq[D], ps: Int = 3, color: C = Color.BLUE, pt: P = PointType.Dot) : Unit
[P: PointTypeLike, C: ColorLike, D: SeqOfDoubleTuples]
```

<a name="zscatter"></a>
```bash
zscatter(data: Seq[D], zValues: Seq[Double], ps: Int = 5,
         colorMap: Double => Color = colormaps.viridis,
         pt: P = PointType.Dot): Unit
[P: PointTypeLike, D: SeqOfDoubleTuples]
```

```scala
object SimplePlotExample {
  // Function that we will plot
  def f(x: Double): Double = Math.sin(x) / x
}
```