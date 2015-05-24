// val sc: SparkContext
// Create an RDD for the vertices
import org.apache.spark._
import org.apache.spark.graphx._
// To make some of the examples work we will also need RDD
import org.apache.spark.rdd.RDD

val users: RDD[VertexId] = sc.parallelize(Array((0L),(1L),(2L),(3L),(4L),(5L),(6L),(7L)))
val relationships: RDD[Edge[Int]] = sc.parallelize(Array(Edge(1L, 0L,1),Edge(1L, 2L,1),Edge(0L, 2L,1), Edge(2L, 3L,1), Edge(3L, 4L,1), Edge(4L, 5L,1), Edge(5L, 7L,1), Edge(3L, 7L,1), Edge(6L, 5L,1), Edge(6L, 7L,1), Edge(6L, 3L,1)))
// val relationships: RDD[Edge] = sc.parallelize(Array(Edge(1L, 0L),Edge(1L, 2L),Edge(0L, 2L), Edge(2L, 3L), Edge(3L, 4L), Edge(4L, 5L), Edge(5L, 7L), Edge(3L, 7L), Edge(6L, 5L), Edge(6L, 7L), Edge(6L, 3L)))

val graph = Graph(users, relationships)

val vertexArray = Array(
  (1L, ("Alice", 28)),
  (2L, ("Bob", 27)),
  (3L, ("Charlie", 65)),
  (4L, ("David", 42)),
  (5L, ("Ed", 55)),
  (6L, ("Fran", 50)),
  (0L, ("2", 22)),
  (7L, ("3", 33))
  )
val edgeArray = Array(
  Edge(0L, 1L, 1),
  Edge(0L, 2L, 1),
  Edge(1L, 2L, 1),
  Edge(2L, 3L, 1),
  Edge(3L, 6L, 1),
  Edge(3L, 4L, 1),
  Edge(4L, 5L, 1),
  Edge(5L, 6L, 1),
  Edge(6L, 7L, 1),
  Edge(7L, 5L, 1),
  Edge(7L, 3L, 1)
  )

val vertexArray = Array(
  (0L, ("Alice", 28)),
  (1L, ("Bob", 27)),
  (2L, ("Charlie", 65)),
  (3L, ("David", 42)),
  (4L, ("Ed", 55)),
  (5L, ("Fran", 50))
  )
val edgeArray = Array(
  Edge(0L, 1L, 1),
  Edge(0L, 2L, 1),
  Edge(1L, 2L, 1),
  Edge(4L, 3L, 1),
  Edge(4L, 5L, 1),
  Edge(3L, 5L, 1)
  )

val vertexRDD: RDD[(Long, (String, Int))] = sc.parallelize(vertexArray)
val edgeRDD: RDD[Edge[Int]] = sc.parallelize(edgeArray)

val graph: Graph[(String, Int), Int] = Graph(vertexRDD, edgeRDD)

val vertexArray = Array((0L),(1L),(2L),(3L),(4L),(5L),(6L),(7L))
val vertexRDD = sc.parallelize(vertexArray)
val edgeArray = Array(Edge(1L, 0L,1),Edge(1L, 2L,1),Edge(0L, 2L,1), Edge(2L, 3L,1), Edge(3L, 4L,1), Edge(4L, 5L,1), Edge(5L, 7L,1), Edge(3L, 7L,1), Edge(6L, 5L,1), Edge(6L, 7L,1), Edge(6L, 3L,1))
val edgeRDD = sc.parallelize(edgeArray)

// similarity matrix
val matrix = Array.ofDim[Double](4,4)
for (i <- 0 to 3){ for ( j <- 0 to 3){ if (i == j){matrix(i)(j)=1.0} } }
val broadcastVar = sc.broadcast(matrix)
// 點
val vertices = sc.parallelize(Array(0,1,2,3))
// 邊
val edges = sc.parallelize(Array((0,1),(0,2),(1,2),(2,3)))
// 找出 neighbor
val neighbor = edges.flatMap(x => Array((x._2,x._1),(x._1,x._2))).groupByKey()
// rdd -> Array
val arrayNeighbor = neighbor.collect().toArray
// vertices use cartesian 找出上三角
val upperTmatrix = vertices.cartesian(vertices).filter{ case (a,b) => a < b }

// 找出要算點的Neighbor
val sNeighbor = upperTmatrix.map(x => arrayNeighbor.filter(y => y._1 ==x._1 || y._1 ==x._2)).map(x => (x(0)._2,x(1)._2))

// 要算點的Neighbor的Neighbor數相乘
val sNeighborLenProduct = sNeighbor.map(x => x._1.size*x._2.size)

// cartesian
implicit class Crossable[X](xs: Traversable[X]) {
  def cross[Y](ys: Traversable[Y]) = for { x <- xs; y <- ys } yield (x, y)
}
// 產生Neighbor的Neighbor Pairs
val sNeighborPairs = sNeighbor.map( x => x._1 cross x._2)

for (i <- 1 to 20){
  val NeighborPairsSimilarity = sNeighborPairs.map(x => x.map(y => matrix(y._1)(y._2)).sum)

  val ans = NeighborPairsSimilarity.zip(sNeighborLenProduct)
  val Similarity = ans.map(x => x._1/x._2*0.9)

  val SimilarityMatrix = upperTmatrix.zip(Similarity)
  SimilarityMatrix.collect().map(x => matrix(x._1._1)(x._1._2)=x._2 )
  for ( i <- 0 to 3){ for ( j <- 0 to 3){ if (j>i){ matrix(j)(i) = matrix(i)(j)} } }
  val broadcastVar = sc.broadcast(matrix)
}

// xs cross ys

// def findNeighbor(args:Int){
//       for ( x <- arrayn ) {
//          println( x._2 )
//       }	
// }
// arrayn.filter(x => x._1 ==4)
// def findNeighbor( a:Int ) :  Int = {for ( x <- arrayn ) {if(a == x._1){return x._1 }}	}
// for ( x <- arrayn ) {println( x._2 )}	


