# StellatedRoller
A hypotrochoid animation generator. Inspired by Mathologer's video on [The 3-4-7 miracle](https://www.youtube.com/watch?v=oEN0o9ZGmOM).

## Running
To run StellatedRoller, simply ensure that you have a JVM installed and do `./gradlew run`.

To tweak the parameters, edit the `StellatedRoller.java` file. A "star label" of {m/n} can be provided to the `Roller` constructor as `new Roller(m, n, 0.45)`.
The final parameter, `pointDistancePercentage`, is the percent of the radius that the points should be placed away from the center. Larger values give larger
intra-polygons.

PRs for a proper GUI are welcome.
