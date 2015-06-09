/* SimpleApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp {
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("Simple Application").set("spark.executor.memory","5g")
    val sc = new SparkContext(conf)
    // label propagation
    def updateNewId(neighbors:Iterable[String],idVertices:Array[(String, String)]): String = {
      var idList = List[String]()
      for ( neighbor <- neighbors){ 
        for ( idV <- idVertices){  
          if (neighbor == idV._1){ idList::= idV._2 } 
          }
        }
      val mostId = idList.groupBy( x => x).maxBy( x => (x._2.size, x._1))
      mostId._1
    }

    // not load file
    // val vertexArray = Array(("0"),("1"),("2"),("3"),("4"),("5"),("6"),("7"))
    // val vertices = sc.parallelize(vertexArray)
    // val edgeArray = Array(("0","1"),("0","2"),("1","2"),("2","3"),("3","6"),("3","4"),("4","5"),("5","6"),("6","7"),("7","5"),("7","3"))
    // val edges = sc.parallelize(edgeArray)

    // load file
    val data = sc.textFile("hdfs://10.0.3.1:9000/in/facebook_combined.txt")
//    val edgeRdd =  data.map(s => ( s.split(",")(0) , s.split(",")(1) ))
//    val vertexRdd = data.flatMap(s => s.split(",")).distinct()
    val edgeRdd =  data.map(s => ( s.split(" ")(0) , s.split(" ")(1) ))
    val vertexRdd = data.flatMap(s => s.split(" ")).distinct()
    // broadcast idVertices
    var idVertices = vertexRdd.map(x => (x,x)).collect
    var bIdVertices = sc.broadcast(idVertices)

    val neighborRdd = edgeRdd.flatMap(x => Array( (x._2,x._1) ,(x._1,x._2) )).groupByKey()

    // (id,dcluster)
    for (i <- 1 to 5){
    val newIdVertices = neighborRdd.map( x => (x._1,updateNewId(x._2, bIdVertices.value))).collect()
    bIdVertices = sc.broadcast(newIdVertices)
    }

    val dCluster = sc.parallelize(bIdVertices.value,8)
    // save community label propagation result
    dCluster.map(x => x._1+","+x._2.mkString("-")).saveAsTextFile("hdfs://10.0.3.1:9000/in/community")

    val dVerCluster = dCluster.map(x => (x._2,x._1)).groupByKey()

    val bEdgeArray = sc.broadcast(edgeRdd.collect())

    def dSameVertexEdges(Vertices:Iterable[String],edgeArray:Array[(String, String)]): List[(String, String)] = {
      var sameedge = List[(String, String)]()
      for ( edge <- edgeArray){ 
        var counter = 0
        for ( vertex <- Vertices){
          if( vertex == edge._1 ){
            counter += 1
          } 
        }
        for ( vertex <- Vertices){
          if( vertex == edge._2 ){
            counter += 1
          } 
        }
        if( counter == 2){
          sameedge::= edge
        }
      }
      sameedge
    }

    val dvertexEdges = dVerCluster.map(x => (x._2,dSameVertexEdges(x._2, bEdgeArray.value)))

    def numVertex(slength:Int): List[Int] = {
      var numVertexList = List[Int]()
      for( i <- 0 to slength-1) {
        numVertexList :+= i
      }
      numVertexList
    }

    def newNumEdges(edges:List[(String, String)],zipVertices:List[(Int, String)]): List[(Int, Int)] = {
      var newEdges = List[(Int, Int)]()
      var mapEdge = Array(0,0)
      for ( edge <- edges){ 
        for ( zipVertex <- zipVertices){  
          if (zipVertex._2 == edge._1){ mapEdge(0) = zipVertex._1} 
          if (zipVertex._2 == edge._2){ mapEdge(1) =  zipVertex._1} 
          }
        newEdges :+= (mapEdge(0),mapEdge(1))
        mapEdge = Array(0,0)
        }
      newEdges
    }

    // cartesian
    implicit class Crossable[X](xs: Traversable[X]) {
      def cross[Y](ys: Traversable[Y]) = for { x <- xs; y <- ys } yield (x, y)
    }

    def simrank(arg1: Iterable[String], arg2:List[(String, String)]): (Array[Array[Double]], List[(Int, String)]) = {
      val slength = arg1.toArray.length
      var matrix = Array.ofDim[Double](slength,slength)

      for (i <- 0 to slength-1){ for ( j <- 0 to slength-1){ if (i == j){matrix(i)(j)=1.0} } }

      var broadcastMatrix = sc.broadcast(matrix)

      val numVertexList = numVertex(slength)

      val zipVertices = numVertexList.zip(arg1)

      val newNumVertices = zipVertices.map(x => x._1)

      val edgesRdd = sc.parallelize(newNumEdges(arg2,zipVertices),8)

      val vertices = sc.parallelize(numVertexList,8)

      val neighbor = edgesRdd.flatMap(x => Array((x._2,x._1),(x._1,x._2))).groupByKey()
      val arrayNeighbor = neighbor.collect().toArray
      val broadcastArrayNeighbor = sc.broadcast(arrayNeighbor)
      
      val upperTmatrix = vertices.cartesian(vertices).filter{ case (a,b) => a < b }
      val sNeighbor = upperTmatrix.map(x => broadcastArrayNeighbor.value.filter(y => y._1 ==x._1 || y._1 ==x._2)).map(x => (x(0)._2,x(1)._2))
      val sNeighborLenProduct = sNeighbor.map(x => x._1.size*x._2.size)
      val sNeighborPairs = sNeighbor.map( x => x._1 cross x._2)

      for (i <- 1 to 10){
        val NeighborPairsSimilarity = sNeighborPairs.map(x => x.map(y => broadcastMatrix.value(y._1)(y._2)).sum)

        val similarityScore = NeighborPairsSimilarity.zip(sNeighborLenProduct)
        val Similarity = similarityScore.map(x => x._1/x._2*0.9)
        var upadteMatrix = broadcastMatrix.value
        val SimilarityMatrix = upperTmatrix.zip(Similarity)
        SimilarityMatrix.collect().map(x => upadteMatrix(x._1._1)(x._1._2)=x._2 )
        for ( i <- 0 to slength-1){ for ( j <- 0 to slength-1){ if (j>i){ upadteMatrix(j)(i) = upadteMatrix(i)(j)} } }
        broadcastMatrix = sc.broadcast(upadteMatrix)
      }
      (broadcastMatrix.value,zipVertices)
    }

    def dSimilarity(dclusters:Array[(Iterable[String], List[(String, String)])] ): List[(Array[Array[Double]], List[(Int, String)])] = {
      var similarityList = List[(Array[Array[Double]], List[(Int, String)])]()
      for( cluster <- dclusters) {
        val similarity = simrank(cluster._1,cluster._2)
        similarityList ::= similarity
      }
      similarityList
    }

    val similarityList = dSimilarity(dvertexEdges.collect())

    def graphSimilarity(similarity: Array[Array[Double]], zipVertices:List[(Int, String)]): List[(String,String,Double)] = {
      val ALength = similarity.length
      var vertex1 = "None"
      var vertex2 = "None"
      var graphVertexSimilarity = List[(String,String,Double)]()
      for ( i <- 0 to ALength-1){ 
        for ( j <- 0 to ALength-1){ 
          if (i < j){ 
            for( zipVertex <- zipVertices) {
              if( i == zipVertex._1){
                vertex1 = zipVertex._2
              }
              if( j == zipVertex._1){
                vertex2 = zipVertex._2
              }
            }
            graphVertexSimilarity :+= (vertex1,vertex2,similarity(i)(j))
          } 
        }
      }
      graphVertexSimilarity 
    }
    val clusterSimilarity = similarityList.flatMap(x => graphSimilarity(x._1,x._2))
    val clusterSimilarityRdd = sc.parallelize(clusterSimilarity,8)

    // save community label propagation result
    clusterSimilarityRdd.map(x => x._1+"-"+x._2+","+x._3).saveAsTextFile("hdfs://10.0.3.1:9000/in/similarity")

  }
}






