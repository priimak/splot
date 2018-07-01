SPlot Basic Example
===================

Below we will plot following example
![Rabbit](http://www.devfortress.xyz/assets/splot-simple-example.png)
Complete code for this example avilable in [SimplePlotExample.scala](https://git.devfortress.xyz/plugins/gitiles/splot/+/master/src/test/scala/xyz/devfortress/splot/examples/SimplePlotExample.scala)

To start using it first import relevant classes and objects
```scala
import xyz.devfortress.splot._
```
We will plot following function and will also need its derivative
```scala
def f(x: Double): Double = Math.sin(x) / x
def df(x: Double): Double = Math.cos(x) / x - Math.sin(x) / (x * x)
```
Create [_Figure_](http://git.devfortress.xyz/plugins/gitiles/splot/+/master/src/main/scala/xyz/devfortress/splot/Figure.scala#24) and plot function f(x) as line plot
using method [_Figure.plot(...)_](http://git.devfortress.xyz/plugins/gitiles/splot/+/master/src/main/scala/xyz/devfortress/splot/Figure.scala#52)
```scala
val fig = Figure()
val fdata: Seq[(Double, Double)] = (0.1 to 20.0 by 0.2).map(x => (x, f(x)))
fig.plot(fdata, lw = 4, color = "orange")
```
Now also plot a few points along this function as crosses. To do that we call [_Figure.scatter(...)_](http://git.devfortress.xyz/plugins/gitiles/splot/+/master/src/main/scala/xyz/devfortress/splot/Figure.scala#65)
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
Now plot three rectangles using [_Figure.rectangle(...)_](http://git.devfortress.xyz/plugins/gitiles/splot/+/master/src/main/scala/xyz/devfortress/splot/Figure.scala#88)
First rectangle has only boundary drawn. First argument is x-y point at the lower 
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
Note that method _plot(...)_, _scatter(...)_ and _rectangle(...)_ are all convinience methods that mimic somewhat 
behaviour of matplotlib. Under the hood they all call method [_Figure.+=(Plot)_](http://git.devfortress.xyz/plugins/gitiles/splot/+/master/src/main/scala/xyz/devfortress/splot/Figure.scala#40).
No such method exist which adds arbitrary closed polygons, instead you have to use explicitly method _+=(Plot)_ and pass
instance of [_Shape_](http://git.devfortress.xyz/plugins/gitiles/splot/+/master/src/main/scala/xyz/devfortress/splot/Plot.scala#83) which is one of the instances of trait _Plot_.

Finally we display window with the plot
```scala
fig.show()
```
You can zoom into arbitrary section of the plot by dragging mouse with left mouse button pressed (zoom rectangle will
be displayed). And you can reset to original (un-zoomed) view by pressing key 'r' on keyboard. To quit you can either
close window or press key 'q'.
