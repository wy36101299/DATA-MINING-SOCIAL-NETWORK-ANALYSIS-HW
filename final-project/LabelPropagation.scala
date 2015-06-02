val vertexArray = Array((0L),(1L),(2L),(3L),(4L),(5L))
val vertices = sc.parallelize(vertexArray)

val idVertices = vertices.map(x => (x,x)).collect
// broadcast idVertices
var bIdVertices = sc.broadcast(idVertices)

val edgeArray = Array((0L,1L),(0L,2L),(1L,2L),(2L,3L),(4L,3L),(4L,5L),(5L,3L))
val edges = sc.parallelize(edgeArray)

val neighbor = edges.flatMap(x => Array((x._2,x._1),(x._1,x._2))).groupByKey()
// val arrayNeighbor = neighbor.collect().toArray


def ee(args1:Iterable[Long],args2:Array[(Long, Long)]): Long = {
	var b = List[Long]()
	for ( a <- args1){ 
		for ( id <- args2){  
			if (a == id._1){ b::= id._2 } 
			}
		}
	b
	var result = b.groupBy( x => x).maxBy( x => (x._2.size, x._1))
	result._1
}

var idVertices = Array((4L,5L), (0L,2L), (1L,2L), (5L,4L), (2L,3L), (3L,5L))
for (i <- 1 to 20){
val cc = neighbor.map( x => (x._1,ee(x._2,idVertices))).collect()
idVertices = cc
}

def search(array: Array[Long]): Long = {
    val result = array.groupBy( x => x).maxBy( x => (x._2.size, x._1))
    result._1
}

Array((4,List(5, 3)), (0,List(2, 1)), (1,List(2, 0)), (5,List(3, 4)), (2,List(3, 1, 0)), (3,List(5, 4, 2)))

