package services.algorithms

import com.google.inject.Inject

import scala.collection.mutable.{ListBuffer, Map => MutableMap}

/**
  * Created by Tawkir Ahmed Fakir on 7/24/2017.
  */
class DFS @Inject()() {

  private var edgeMap = MutableMap.empty[Int, List[Int]]
  // TODO: Convert the flag to enum
  private var visitFlag = MutableMap.empty[Int, Int]
  // 1 means visit start, 2 means end.
  private var sortOrder = scala.collection.mutable.ListBuffer.empty[Int]

  def getTopSortWithIndependentNodes(startNodeList: Seq[Int], edgeList: List[(Int, Int)]): Seq[Seq[Int]] = {

    val unDirectedEdgeMap = getUndirectedEdgeMap(edgeList)
    val subGroups = getRelatedNodes(startNodeList, unDirectedEdgeMap)

    val directedEdgeMap = edgeList.groupBy(_._1).map(x => x._1 -> x._2.map(_._2))
    for (subGroup <- subGroups) yield getTopSort(subGroup, collection.mutable.Map(directedEdgeMap.toSeq: _*))
  }

  private def getTopSort(startNodeList: Seq[Int], edgeMap: MutableMap[Int, List[Int]]): Seq[Int] = {
    initFlags()

    this.edgeMap = edgeMap
    startNodeList.foreach(node => {
      if (getFlag(node) == 0) visitNode(node, enableCycleCheck = true)
    })

    this.sortOrder.reverse
  }

  private def getRelatedNodes(startNodeList: Seq[Int], edgeMap: MutableMap[Int, List[Int]]): Seq[Seq[Int]] = {
    initFlags()

    this.edgeMap = edgeMap

    (for (node <- startNodeList) yield {
      if (getFlag(node) == 0) {
        visitNode(node, enableCycleCheck = false)
        val currentNodes: ListBuffer[Int] = this.sortOrder.reverse
        this.sortOrder.clear()
        currentNodes
      } else {
        Seq.empty[Int]
      }
    }).filter(x => x.nonEmpty)
  }

  private def initFlags(): Unit = {
    this.edgeMap = MutableMap.empty[Int, List[Int]]
    this.visitFlag = MutableMap.empty[Int, Int] // 1 means visit start, 2 means end.
    this.sortOrder = scala.collection.mutable.ListBuffer.empty[Int]
  }

  private def getUndirectedEdgeMap(edgeList: List[(Int, Int)]): MutableMap[Int, List[Int]] = {
    val edgeMap = MutableMap.empty[Int, List[Int]]

    edgeList.foreach(edge => {
      val (to, from) = edge
      val toVal = from :: edgeMap.getOrElse(to, List.empty[Int])
      edgeMap.put(to, toVal)

      val fromVal = to :: edgeMap.getOrElse(from, List.empty[Int])
      edgeMap.put(from, fromVal)
    })

    edgeMap
  }

  private def visitNode(node: Int, enableCycleCheck: Boolean): Unit = {
    val nodeStatus = getFlag(node)

    if (enableCycleCheck && nodeStatus == 1) throw new Exception("Scheduling is not possible as cyclic dependencies exist!!!")

    if (nodeStatus == 0) {
      setFlag(node, 1)
      this.edgeMap.getOrElse(node, Seq.empty[Int]).foreach(edge => {
        visitNode(edge, enableCycleCheck)
      })
      setFlag(node, 2)
      this.sortOrder += node
    }
  }

  private def setFlag(node: Int, value: Int): Unit = this.visitFlag.put(node, value)

  private def getFlag(node: Int): Int = if (!this.visitFlag.contains(node)) 0 else this.visitFlag(node)
}

