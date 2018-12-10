# PointPlot

Plot shown as set of x-y points (i.e. a scatter plot).

See [Figure::scatter(...)](Figure.md#scatter) for convenience function that you can use instead
adding this object directly to the figure. Method `Figure::scatter(...)` uses implicit conversion
methods that make its use significantly more convenient.

## Constructor

Following parameters (shown with default values) can be passed to the `PointPlot` constructor:

```bash
data: Seq[(Double, Double)]
```
Set of `(x,y)` points to be drawn.

```bash
pointSize: Int = 3
```
Size of point in pixels.

```bash
color: Color = Color.BLUE
```
Color of points.


```bash
pointType: PointType = PointType.Dot
```
Type of points to be drawn. Possible values are `PointType.{Dot, Cross, Circle)`



