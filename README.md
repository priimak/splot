![SPlot Logo](http://www.devfortress.xyz/assets/splot-logo.png)

 Scala 2D Plotting library.
===========================
[![Build Status](https://travis-ci.com/priimak/splot.svg?branch=master)](https://travis-ci.com/priimak/splot)
[![codecov.io](http://codecov.io/github/priimak/splot/coverage.svg?branch=master)](http://codecov.io/github/priimak/splot/coverage.svg?branch=master)
[![License](https://img.shields.io/:license-MIT-blue.svg)](https://raw.githubusercontent.com/priimak/splot/master/LICENSE)

_Version: 0.4.0-SNAPSHOT_

[Roadmap](http://git.devfortress.xyz/plugins/gitiles/splot/+/master/ROADMAP.md).

Documentation for latest release version (0.2.0) is available [here](https://git.devfortress.xyz/plugins/gitiles/splot/+/rel-0.2.0-doc/README.md).

_SPlot_ is a poor-man replacement of great [matplotlib](https://matplotlib.org/) Python library for Scala.

Source for the library is available through anonymous http.
```bash
git clone http://git.devfortress.xyz/splot
```

Current version of this library is available on maven central at these coordinates

```bash
libraryDependencies += "xyz.devfortress.splot" % "splot-core_2.12" % "0.3.0"
```
```bash
<dependency>
    <groupId>xyz.devfortress.splot</groupId>
    <artifactId>splot-core_2.12</artifactId>
    <version>0.3.0</version>
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
