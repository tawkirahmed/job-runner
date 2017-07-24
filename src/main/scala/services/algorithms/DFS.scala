package services.algorithms

import com.google.inject.Inject

/**
  * Created by Tawkir Ahmed Fakir on 7/24/2017.
  */
class DFS @Inject()() {

  private var edgeMap = Map.empty[Int, scala.Seq[Int]]
  // TODO: Convert the flag to enum
  private val visitFlag = scala.collection.mutable.Map.empty[Int, Int] // 1 means visit start, 2 means end.
  private var sortOrder = scala.collection.mutable.ListBuffer.empty[Int]

  def getTopSort(startNodeList: Seq[Int], edgeMap: Map[Int, scala.Seq[Int]]): Seq[Int] = {
    this.edgeMap = edgeMap
    startNodeList.foreach(node => {
      if (getFlag(node) == 0) {
        println(node)
        visitNode(node)
      }

    })
    sortOrder.toSeq.reverse
  }

  private def visitNode(node: Int): Unit = {
    val nodeStatus = getFlag(node)
    if (nodeStatus == 1) throw new Exception("Scheduling is not possible as cyclic dependencies exist!!!")
    if (nodeStatus == 0) {
      setFlag(node, 1)
      edgeMap.getOrElse(node, Seq.empty[Int]).foreach(edge => {
        visitNode(edge)
      })
      setFlag(node, 2)
      sortOrder += node
    }
  }

  private def setFlag(node: Int, value: Int): Unit = visitFlag.put(node, value)

  private def getFlag(node: Int): Int = if (!visitFlag.contains(node)) 0 else visitFlag(node)
}
