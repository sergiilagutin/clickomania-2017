package duel

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

object StrategyTest {

  trait Strategy extends (Seq[Component] => Seq[Component])

  def strategies: Array[AnyRef] =
    Array(
      Array(nothing, "doNothing"),
      Array(byMaxX, "byMaxX"),
      Array(byMaxXMinY, "byMaxXMinY"),
      Array(byMaxXMaxY, "byMaxXMaxY"),
      Array(byX, "byX"),
      Array(byYReverse, "byYReverse"),
      Array(byMaxXDistance, "byMaxXDistance"),
      Array(custom, "custom"),
    )

  private def nothing: Strategy = (cs: Seq[Component]) => cs

  private def colorOrdering(cs: Seq[Component]): Ordering[Component] = {
    val colorMap = cs.groupBy(_.color).mapValues(_.size)
    (x: Component, y: Component) => colorMap(x.color) - colorMap(y.color)
  }

  private def reverseInt = Ordering.Int.reverse

  private def byMaxX: Strategy = (cs: Seq[Component]) => cs.sortBy(_.maxX)

  private def byMaxXMinY: Strategy = (cs: Seq[Component]) =>
    cs.sortBy(c => (c.maxX, c.minY))

  private def byMaxXMaxY: Strategy = (cs: Seq[Component]) =>
    cs.sortBy(c => (c.maxX, c.maxY))

  private def byX: Strategy = (cs: Seq[Component]) => cs.sortBy(_.x)

  private def byYReverse: Strategy = (cs: Seq[Component]) => cs.sortBy(_.y)(reverseInt)

  private def byMaxXDistance: Strategy = (cs: Seq[Component]) =>
    cs.sortBy(c => (c.maxX, c.distanceToZero))(Ordering.Tuple2(Ordering.Int, Ordering.Int))

  private def sameColor(color: Int): Ordering[Component] =
    (x: Component, y: Component) =>
      if (x.color == color) -1
      else if (y.color == color) 1
      else 0

  private def custom: Strategy = (cs: Seq[Component]) => {
    val completedColor = cs
      .groupBy(_.color)
      .toList
      .find {
        case (_, components) => components.forall(_.cellCount > 1)
      }

    completedColor match {
      case Some((color, _)) =>
        byMaxXDistance(
          cs.sorted(sameColor(color).reverse))
      case None => byMaxXDistance(cs)
    }
  }
}

class StrategyTest {
  private val solver = new Solver

  @MethodSource(Array("strategies"))
  @ParameterizedTest
  def testFive(strategy: Seq[Component] => Seq[Component], name: String): Unit = {
    val boards = Boards.five.map(array => new Board(5, array, strategy))
    estimate(10, boards, name)
  }

  @MethodSource(Array("strategies"))
  @ParameterizedTest
  @Disabled
  def testSix(strategy: Seq[Component] => Seq[Component], name: String): Unit = {
    val boards = Boards.six.take(1).map(array => new Board(6, array, strategy))
    estimate(10, boards, name)
  }

  private def estimate(testAmount: Int, boards: Seq[Board], name: String): Unit = {
    var min = Long.MaxValue
    var max = Long.MinValue
    var total = 0L

    for {
      _ <- 1 to testAmount
    } {
      val start = System.currentTimeMillis()
      boards.foreach(solver.findSolution)
      val end = System.currentTimeMillis()
      val time = end - start
      if (time < min) min = time
      if (time > max) max = time
      total += time
    }
    println(s"$name avg: ${total / testAmount}; min: $min; max: $max ms")
  }

}
