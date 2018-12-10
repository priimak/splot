# ZPointPlot

ZPoint plot is very similar to [PointPlot](PointPlot.md) but also contains
sequence of zValues that should be in range from 0 to 1 inclusive and color
of each point is determined by by calling provided colorMap function which
takes value from zValues for a given point and returns color to be used for
plotting this point.

See [Figure::zscatter(...)](Figure.md#zscatter) for convenience function
that you can use instead adding this object directly to the figure.
Method `Figure::zscatter(...)` uses implicit conversion methods that make
its use significantly more convenient.

## Constructor

Following parameters (shown with default values) can be passed to the `ZPointPlot` constructor:

```bash
data: Seq[(Double, Double)]
```

```bash
zValues: Seq[Double]
```

```bash
pointSize: Int = 5
```

```bash
colorMap: Double => Color = colormaps.viridis
```

```bash
pointType: PointType = PointType.Dot
```