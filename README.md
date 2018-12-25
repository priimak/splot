![SPlot Logo](http://www.devfortress.xyz/assets/splot-logo.png)

 Scala 2D Plotting library.
===========================
[![Build Status](https://travis-ci.com/priimak/splot.svg?branch=master)](https://travis-ci.com/priimak/splot)
[![codecov.io](http://codecov.io/github/priimak/splot/coverage.svg?branch=master)](http://codecov.io/github/priimak/splot/coverage.svg?branch=master)
[![License](https://img.shields.io/:license-MIT-blue.svg)](https://raw.githubusercontent.com/priimak/splot/master/LICENSE)

_Version: 0.4.0-SNAPSHOT_

Documentation for latest release version (0.4.0) is available at [splot-web.github.io](https://splot-web.github.io/).

_SPlot_ is a poor-man replacement of [matplotlib](https://matplotlib.org/) Python library for Scala.

Current version of this library is available on maven central at these coordinates

```bash
libraryDependencies += "xyz.devfortress.splot" % "splot-core_2.12" % "0.4.0"
```

It can plot line plots, scatter plots, arbitrary closed polygons, heatmap plots etc.

To try it out type following into your [Ammonite-REPL](https://ammonite.io/#Ammonite-REPL)

```scala
import $ivy.`xyz.devfortress.splot::splot-core:0.4.0`, xyz.devfortress.splot._
import Math._
import java.util.Random

def f(x: Double)(implicit rnd: Random): Double =
  (rnd.nextDouble * 0.3  + 1) * sin(x + 0.1 * (rnd.nextDouble - 0.5))

val fig = new Figure(
  title = "Periodic signal with random phase shift and amplitude",
  xLabel = "t - time",
  yLabel = "Signal Level",
  showGrid = true
)

implicit val rnd = new Random
val data = (0.0 to 20 by 0.1).map(x => (x, f(x)))

fig.plot(data, lw = 2, color = "blue")
fig.show(800, 600)
```