![SPlot Logo](http://www.devfortress.xyz/assets/splot-logo.png)

 Scala 2D Plotting library.
===========================
_Version: 0.2.0_

Documentation for current snapshot is available [here](https://git.devfortress.xyz/plugins/gitiles/splot/+/master/README.md).

_SPlot_ is a poor-man replacement of great [matplotlib](https://matplotlib.org/) Python library for Scala.
Source for the library is available through anonymous http.
```bash
git clone http://git.devfortress.xyz/splot
```

Current version of this library is available on maven central at these coordinates

```bash
libraryDependencies += "xyz.devfortress.splot" % "splot-core_2.12" % "0.2.0"
```
```bash
<dependency>
    <groupId>xyz.devfortress.splot</groupId>
    <artifactId>splot-core_2.12</artifactId>
    <version>0.2.0</version>
</dependency>
```

It can plot line plots, scatter plots, arbitrary closed polygons etc.

* [Simple Example](EXAMPLE.md)
* Documentation
  1. [Figure/new](http://git.devfortress.xyz/plugins/gitiles/splot/+/rel-0.2.0/src/main/scala/xyz/devfortress/splot/Figure.scala#12)
  2. [Figure/+=Plot](http://git.devfortress.xyz/plugins/gitiles/splot/+/rel-0.2.0/src/main/scala/xyz/devfortress/splot/Figure.scala#37)
  2. [Figure/plot](http://git.devfortress.xyz/plugins/gitiles/splot/+/rel-0.2.0/src/main/scala/xyz/devfortress/splot/Figure.scala#45)
  3. [Figure/scatter](http://git.devfortress.xyz/plugins/gitiles/splot/+/rel-0.2.0/src/main/scala/xyz/devfortress/splot/Figure.scala#57)
  4. [Figure/rectangle](http://git.devfortress.xyz/plugins/gitiles/splot/+/rel-0.2.0/src/main/scala/xyz/devfortress/splot/Figure.scala#76)
