# LinePlot

Sequence of points connected by line.

See [Figure::plot(...)](Figure.md#plot) for convenience function that you can use instead
adding this object directly to the figure. Method `Figure::plot(...)` uses implicit conversion
methods that make its use significantly more convenient.

## Constructor

Following parameters (shown with default values) can be passed to the `LinePlot` constructor:

```bash
data: Seq[(Double, Double)]
```
Sequence of `(x,y)` points forming vertices of the line plot.

```bash
color: Color = Color.BLACK
```
Color to be used for the line.

```bash
lineWidth: Int = 1
```
Width of the line in pixels.

