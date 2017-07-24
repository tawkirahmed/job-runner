package services.algorithms

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

/**
  * Created by Tawkir Ahmed Fakir on 7/24/2017.
  */
class DFSSpec extends FlatSpec {

  val dfs = new DFS()

  "DFS" should "work on one set of orderings properly" in {

    val startNodes = Seq(1, 2)
    val edges = List(
      1 -> 3,
      1 -> 4,
      2 -> 3,
      2 -> 4
    )

    val orderedNodes = dfs.getTopSortWithIndependentNodes(startNodes, edges)

    orderedNodes.size should equal(1)
    orderedNodes.head should contain inOrderOnly(2, 1, 4, 3)
  }

  "DFS" should "work on multiple set of orderings properly" in {

    val startNodes = Seq(1, 2, 5)
    val edges = List(
      1 -> 3,
      1 -> 4,
      2 -> 3,
      2 -> 4,
      5 -> 6
    )

    val orderedNodes = dfs.getTopSortWithIndependentNodes(startNodes, edges)

    orderedNodes.size should equal(2)
    orderedNodes(0) should contain inOrderOnly(2, 1, 4, 3)
    orderedNodes(1) should contain inOrderOnly(5, 6)
  }
}
