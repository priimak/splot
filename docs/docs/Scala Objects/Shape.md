title:      Shape
date:       2018/10/12
template:   document
nav:        Scala Objects>Shape __4__

Plot shown as a closed polygon shape.

There are no convenience functions for it in `Figure`, but method
[Figure::rectangle(...)](Figure.md#rectangle) does use this object to
draw rectangles.

## Constructor

Following parameters (shown with default values) can be passed to the `Shape`
constructor:

```bash
data: Seq[(Double, Double)]
```

```bash
color: Color = Color.BLUE
```

```bash
lineWidth: Int = 1
```

```bash
fillColor: Option[Color] = None
```



