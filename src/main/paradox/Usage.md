# Usage

First import all of the SPlot classes
```scala
import xyz.devfortress.splot._
```
Or in Ammonite-REPL
```scala
import $ivy.`xyz.devfortress.splot::splot-core:0.4.0`, xyz.devfortress.splot._
```
This will bring all of the important implicits into the scope without which
this library may not work.

## Figure
Central class in the SPlot library is `Figure` to which plot elements are added
and then displayed. Every figure constructor parameter is populated with default
values and you can just create it like so

```scala
val fig = new Figure()
```

However it is likely that you will not be satisfied with defaults and therefore
will most likely be supplied different values. For example following code
settings up plot title, labels and on both axis and requests to display grid.

```scala
val fig = new Figure(
  title = "Signal of ...",
  xLabel = "t - time in seconds",
  yLabel = "Signal strength",
  showGrid = true
)
```
Another common thing to change is domain and range of the plot. By default
they are set to `None` which means that domain and range are automatically derived
from added plot elements. Often however you may want to have fixed domain and/or range
which is done by adding `(min, max)` tuples to `Figure.domain|range` values.

```scala
val fig = new Figure(
  domain = (-10, 10),
  range = (0.0, 3.0)
)
```
Ticks on both axis are automatically derived by means of default functions supplied to
`xTicks` and `yTicks` arguments. They are functions that receive range along corresponding
axis and return sequence of `(Double, String)` tuples where first element is a position at
which tick is to drawn and second a corresponding label. Several utility function are
available to make it easier to supply custom ticks. To disable all ticks you can use
class `Ticks(...)` without any arguments.

```scala
val fig = new Figure(xTicks = Ticks(), yTicks = Ticks())`
```

Supplying sequence of numbers or ranges to `Ticks(...)` will create ticks at predefined
positions

```scala
val fig new Figure(
  xTicks = Ticks(0, 1, 2, 4, 8, 16),
  yTicks = Ticks(0.0 to 10 by 1.5)
)
```

Plot elements are added to the figure by calling

```scala
fig.add(plotElement: PlotElement)
```

But for many plot elements convenience functions exist in `Figure` that make adding
them much easier.

## Line Plot

Like most of the plot element line plot is derived from sequence of x-y
tuples `(Double, Double)` where first element is coordinate position along
the x-axis and second along the y-axis. Thus given a sequence of data points

```scala
val data = Seq(...)
```

You can plot line plot where every two subsequent points are connected by a line using
convenience function `Figure::plot(...)`

```scala
fig.plot(data)
```

By default black color is used for all plot element including line plot. Default line
width is 1 and line type is a solid line. That can be changed like so

```scala
fig.plot(data, lw = 2, color = "blue", lt = "--")
```

Parameter `lw` stands for "line width" and defines width of the line in pixels.

Parameter `color` is actually of type `java.awt.Color` but following colors can be passed by
implicit conversion from strings `"red"`, `"green"`, `"blue"`, `"black"`, `"cyan"`, `"gray"`
`"lightgray"`, `"magenta"`, `"orange"`, `"pink"`, `"white"`, `"yellow"`.

Likewise line type ("lt") is of type `LineType` and can be one of `SOLID`, `DASHES`,
`DASHES_AND_DOTS`, `DOTS` and that can also be expressed by corresponding strings
`"-"`, `"--"`, `"-."`, `"."`

Alternatively you can use `LinePlot` class which is a `PlotElement` and thus can be added
to figure like so

```scala
fig.add(LinePlot(data))
```

## Scatter Plot

Scatter plot display data as set of points. It can be added by using `PointPlot` class:

```scala
fig.add(PointPlot(...))
```

or using convenience function `Figure::scatter(...)`

```scala
fig.scatter(data)
```

Several different point types/shapes can be used with default being `PointType.Dot`.
For example following will plot scatter plot using unfilled circles of radius 9 pixels.

```scala
fig.scatter(data, pt = "o", ps = 9)
```

where `pt` stands for "_point type_" and `ps` for "_point size_". Like for color for
point type there should be in scope explicit conversion from string into `PointType`
instances, which are `Dot`, `Cross`, `X`, `Square`, `Diamond` and `Circle` and their
corresponding string names `"."`, `"+"`, `"x"`, `"s"`, `"d"` and `"o"`. Square, diamond
and circle are drawn as unfilled shapes, which means that they are drawn as outlines
and parameter `color` refers to the color of that outline. To make them filled objects
one needs to set `fc` (fill color) parameter which by default is set to `None`. In
addition you can set transparency of the fill color by changing `fa` (fill alpha) which
varies from 0.0 (full transparency) to 1.0 (fully opaque, default value). This value of
alpha however does not affect transparency outline. If outline is too obscuring it can be
made filly transparent while fill semi-transparent like so.

```scala
fig.scatter(data, pt = "d", ps = 9, fc = "blue", fa = 0.01, color = new Color(0, 0, 0, 0))
```

where 4th parameter passed to `Color` constructor is alpha meaning fully transparent.

`PointType.Dot` is a special type drawn as just filled square without outline and setting
`fa` for such plots does affect its transparency while `fc` parameter has no effect.

## ZScatter Plot

Similar to scatter plot color of the point is derived from supplied colormap and `zValue` array.

## Rectangle

Rectangle plot is a variant of `Shape` plot that draws rectangles. At minimum one needs to
supply anchor point which is a lower left corner of the square and its width and height.

```scala
fig.rectangle(anchor = (5, 6), width = 2, height = 3)
```

By default rectangle will be drawn as outline or solid line width 1 with blue color.
All of these parameters can be explicitly set

```scala
fig.rectangle(
  anchor = (5, 6),
  width = 2,
  height = 3,
  lw = 2,
  lt = "-.", // dashes and dots
  fillColor = "yellow",
  alpha = 0.2 // semi-transparent
)
```

## Bar Plot

Bar plot is a composite plot assembled from many rectangles. In order to display properly
it might be required to pass data sequence sorted along the x-axis. For each point it draws
filled rectangle from 0 on the y-axis up to the y value for this point. By default width of
these bars/rectangles fills up to the next point, i.e. there are no gaps between bars.

```scala
fig.barplot(data)
```

Default fill color is yellow and edge color is black drawn as solid lines of width 1. Fill
color is fully opaque. These parameters can be explicitly set though

```scala
fig.barplot(data, color = "cyan", edgeColor = "blue", edgeWidth = 2, alpha = 0.1)
```

Notice that here `color` parameter sets fill color.

By setting `edgeWidth` to 0 one can remove outline altogether.

Width of the bars and if there is gap between them is controller by `width` parameter
which is a function that defines width of the bar. For each bar it receives distance
to the next data point and returns distance to be used for drawing current bar.
Default function is identity, which means that bars are drawn edge to edge without any
space between bars. Thus we can introduce fixed width for bars like so

```scala
fig.barplot(data, width = _ => 0.7)
```

and flexible gap between bars like so

```scala
fig.barplot(data, width = w => 0.7 * w)
```

## Heat-map Plot

Heatmap like plot is made by adding `ZMapPlot` object to the figure or using convenience
function `map(...)`. This plot is defined at minimum by supplying
`f: (Double, Double) => Double` function that maps arbitrary point in xy-plane into
some value and parameters `xDomain: (Double, Double)` and `yDomain: (Double, Double)`
that define rectangular overarching domain on which supplied function `f(x, y)` is
assumed to be defined. This domain can farther be shaped/reduced by use of `inDomain`
function (see below).

At any zoom level for every pixel of the plot x and y coordinates are computed and
assuming that they fall within defined domain z value `f(x,y)` is computed
and a color picked from provided colormap (default is viridis).Thus assuming that
`f(x,y)` function is defined simplest map plot defined on a certain rectangle can
be drawn like so

```scala
def f(x: Double, y:Double): Double = { ... }

fig.map(f, xDomain = (0, 10), yDomain = (0, 1))
```

By default at the time of plotting this plot range of the z-values will be recorded
and complete color map range will be mapped to this range. This sometimes can lead
to abrupt changes in color when varying configuration parameters of `f` function.
To fix mapping of z values to colormap range one can pass explicitly `zRange` value.
For example following code maps range of z-values from 0 to 10 into provided colormap.

```scala
fig.map(
  f, xDomain = (0, 10), yDomain = (0, 1),
  zRange = (0, 10)
)
```

Note that doing so clips color value derived from z-values that fell outsize of this
range to the lower and upper bounds of colormap.

Colormap is a function `Double => java.awt.Color` that maps numbers in range
`[0,1]` into some color. By default `map(...)` receives `viridis` colormap. The only
other available colormap is `inferno`. It can be set like so:

```scala
fig.map(
  f, xDomain = (0, 10), yDomain = (0, 1),
  colorMap = colormaps.inferno
)
```

Setting `inDomain` parameter to custom function allow farther restrict domain
of the plot and make it for example non-rectangular domains. For example we can
define square domain with round hole of diameter 1 like so:

```scala
fig.map(
    f, xDomain = (-10, 10), yDomain = (-10, 10),
    inDomain = (x, y) => x * x + y * y > 1
)
```

## Shape Plot

Arbitrary closed shapes can be drawn by adding `Shape` objects to `Figure`. No convenience
function exist to aid in their creation.

