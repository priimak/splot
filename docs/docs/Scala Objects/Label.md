title:      Label
date:       2018/10/12
template:   document
nav:        Scala Objects __2__ >Label __100__
percent:    100

Text label that be drawn on the figure. It is always anchored at lower left corner of the
bounding box of the text.

## Constructor

Following parameters (shown with default values) can be passed to the `Label` constructor:

```bash
text: String
```
Text of the label to be drawn.

```bash
x, y: Double
```
X and Y coordinates where label will be drawn.

```bash
font: Font = Font.getFont(Font.SANS_SERIF)
```
Font to be used for the label.

```bash
color: Color = Color.BLACK
```
Color of the text.