title:      ZMapPlot
date:       2018/10/12
template:   document
nav:        Scala Objects>ZMapPlot __6__

Heat map plot of certain `zFunction: (Double, Double) => Double` that maps
points in `(x,y)` plane into `z`-values. This plot draws every pixel within
arbitrarily defined domain.

See [Figure::map(...)](Figure.md#zscatter) for convenience function
that you can use instead adding this object directly to the figure.
Method `Figure::map(...)` uses implicit conversion methods that make
its use significantly more convenient.

## Constructor

Following parameters (shown with default values) can be passed to the `ZMapPlot` constructor:

```bash
zFunction: (Double, Double) => Double
```
Function z=f(x,y) for which heatmap-like plot is to be defined.

```bash
domain: (Double, Double)
```
Inclusive domain of x-values on which f(x,y) is defined

```bash
range: (Double, Double)
```
Inclusive domain of y-values on which f(x,y) is defined.

```bash
zRange: (Double, Double) = (0, 0)
```
Range of z-values produced by f(x,y) function; default range is (0,0) which means that zRange will
be computed automatically. If zRage is smaller than actual range of shown function then colors from the
colormap will be clipped.

```bash
colorMap: Double => Color = colormaps.viridis
```
Colormap which transforms each z-value into color.

```bash
inDomain: (Double, Double) => Boolean
```
Domain function which can be used to define arbitrary domains in `(x,y)` plane where
plotting on the function `f(x,y)` will take place; by default it is assigned to the
special function `derivedDomain` which means that it will be plotted on `[xDomain, yDomain]`
rectangle.



