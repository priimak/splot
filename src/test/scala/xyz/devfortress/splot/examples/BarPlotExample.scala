package xyz.devfortress.splot.examples

import java.awt.Color

import xyz.devfortress.splot._

object BarPlotExample {
  def main(args: Array[String]): Unit = {
    val fig = Figure(
      title = PlainText("Temperature by the hour for 'Stovepipe Wells' for two different dates."),
      titleFontSize = 15,
      xTicks = Ticks(0 to 24),
      yTicks = Ticks(0 to 30 by 5),
      xLabel = "Time of the day",
      yLabel = "Temperature in C",
      leftPadding = 60,
      rightPadding = 60,
      topPadding = 60,
      bottomPadding = 60
    )

    val temp2018Jan01 = Seq(
      (0.0, 10.0), (1.0, 9.5), (2.0, 8.4), (3.0, 8.4), (4.0, 6.9), (5.0, 5.7), (6.0, 7.5), (7.0, 7.0), (8.0, 6.6),
      (9.0, 8.7), (10.0, 9.8), (11.0, 12.7), (12.0, 14.0), (13.0, 15.4), (14.0, 16.4), (15.0, 16.9), (16.0, 17.2),
      (17.0, 16.6), (18.0, 14.4), (19.0, 13.6), (20.0, 12.5), (21.0, 12.3), (22.0, 9.9), (23.0, 10.9)
    )

    val temp2018May01 = Seq(
      (0.0, 23.1), (1.0, 22.5), (2.0, 22.0), (3.0, 21.8), (4.0, 21.5), (5.0, 21.1), (6.0, 21.2), (7.0, 21.9),
      (8.0, 22.5), (9.0, 23.4), (10.0, 24.1), (11.0, 24.7), (12.0, 24.7), (13.0, 23.2), (14.0, 24.5), (15.0, 24.6),
      (16.0, 23.4), (17.0, 22.8), (18.0, 22.4), (19.0, 22.5), (20.0, 19.7), (21.0, 19.2), (22.0, 19.3), (23.0, 19.0)
    )

    // draw barplots of temperature for two different dates
    fig.barplot(temp2018May01, color = Color.YELLOW)
    fig.barplot(temp2018Jan01, width = _ => 0.6, color = Color.BLUE)

    // manually draw legends box
    fig.rectangle(anchor = (18.0, 25.0), width = 5.6, height = 4, fillColor = Color.LIGHT_GRAY, color = "black")
    fig.rectangle(anchor = (18.5, 25.5), width = 1, height = 1, fillColor = "yellow", lw = 0)
    fig.rectangle(anchor = (18.5, 27.0), width = 1, height = 1, fillColor = "blue", lw = 0)
    fig.add(Label("May/01/2018", 20, 25.8))
    fig.add(Label("Jan/01/2018", 20, 27.3))

    fig.show(730, 500)
  }
}
