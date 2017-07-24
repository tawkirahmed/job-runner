package services.algorithms

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

/**
  * Created by Tawkir Ahmed Fakir on 7/24/2017.
  */
class DFSSpec extends FlatSpec {

  "DFS" should "work on one set of orderings properly" in {

    val startNodes = Seq(1, 2)
    val edges = Map(
      1 -> Seq(3, 4),
      2 -> Seq(3, 4)
    )
    val orderedNodes = new DFS().getTopSort(startNodes, edges)

    orderedNodes should contain inOrderOnly(2, 1, 4, 3)
  }

  "DFS" should "work on multiple set of orderings properly" in {

    val startNodes = Seq(1, 2, 5)
    val edges = Map(
      1 -> Seq(3, 4),
      2 -> Seq(3, 4),
      5 -> Seq(6)
    )
    val orderedNodes = new DFS().getTopSort(startNodes, edges)

    orderedNodes should contain inOrderOnly(2, 1, 4, 3)
  }
}
