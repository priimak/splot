SPlot - Scala 2d Plotting library
=================================

_Splot_ is a poor-man replacement of great [matplotlib](https://matplotlib.org/) Python library for Scala.

It can plot line plots, scatter plots and arbitrary closed polygons. 
Below we will plot following example
![Rabbit](http://www.devfortress.xyz/assets/splot-simple-example.png)
To start using it first import relevant classes and objects
```scala
import xyz.devfortress.splot._
```
We will plot following function and will also need its derivative
```scala
def f(x: Double): Double = Math.sin(x) / x
def df(x: Double): Double = Math.cos(x) / x - Math.sin(x) / (x * x)
```
Create _Figure_ and plot function f(x) as line plot
```scala
val fig = Figure()
val fdata: Seq[(Double, Double)] = (0.1 to 20.0 by 0.2).map(x => (x, f(x)))
fig.plot(fdata, lw = 4, color = "orange")
```
Now also plot a few points along this function as crosses.
```scala
fig.scatter((0.2 to 20.0 by 0.6).map(x => (x, f(x))), ps = 20, pt = "+", color = "black")
```
Compute derivatives at following points.
```scala
val x = Seq(4.9, 6.8, 8)
```
Plot derivatives as tangent lines.
 ```scala
x.foreach(x => {
  fig.plot(Seq((x - 3, f(x) - 3 * df(x)), (x + 3, f(x) + 3 * df(x))))
})
```
Draw filled circles at points where we take derivative.
```scala
import java.awt.Color
fig.scatter(x.map(x => (x, f(x))), pt = "o", ps = 20, color = Color.RED)
```
Now plot three rectangles. First rectangle has only boundary drawn. First argument is x-y point at the lower 
left corner.
```scala
fig.rectangle((15, 0.3), 1, 0.3)
```
Second rectange has boundaries drawn as lines and also filled with blue (transparency used by default is 0.2)
```scala
fig.rectangle((13.7, 0.34), 3, 0.1, fillColor = "blue")
```
Third rectangle will not have boundaries drawn, only filled with color red and custom transparency of 0.6
```scala
fig.rectangle((13.3, 0.38), 2.6, 0.2, fillColor = "red", lw = 0, alpha = 0.6)
```
Now draw arbitrary polygon shape in outline of black lines of lineWidth 5 and filled in nontransparent pink
```scala
fig += Shape(
  Seq((8.2, 0.6), (12, 0.5), (9.4, 0.95), (8.8, 0.93)),
  fillColor = Some(Color.PINK), color = Color.BLACK, lineWidth = 5
)
```
And finally display window with the plot
```scala
fig.show()
```