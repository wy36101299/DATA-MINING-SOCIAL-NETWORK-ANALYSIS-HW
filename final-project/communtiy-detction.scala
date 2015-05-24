object LabelPropagation {

	def run[ED: ClassTag](graph: Graph[_, ED], maxSteps:Int):  Graph[VertexId, ED]= {
		
		val lpaGraph = graph.mapVertices { case (vid, _) => vid  }

		def sendMessage(e: EdgeTriplet[VertexId,ED]) = {
			Iterator((e.srcId, Map(e.dstAttr -> 1L)),(e.dstId, Map(e.srcAttr -> 1L)))
		}

		def mergeMessage(count1: Map[VertexId, Long], count2: Map[VertexId, Long]): Map[VertexId, Long] = {
			(count1.keySet ++ count2.keySet).map { i =>
				val count1Val = count1.getOrElse(i, 0L)
				val count2Val = count2.getOrElse(i, 0L)
				i -> (count1Val + count2Val)
			}.toMap

		def vertexProgram(vid: VertexId, attr: Long, message: Map[VertexId, Long]) = {
			if (message.isEmpty) attr else message.maxBy(_._2)._1
		}

		val initialMessage = Map[VertexId, Long]()

		Pregel(lpaGraph, initialMessage, maxIterations = maxSteps)(
			vprog = vertexProgram,
			sendMsg = sendMessage,
			mergeMsg = mergeMessage)
		}
	}
	
}