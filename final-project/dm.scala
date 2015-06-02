
def returnNewId(args1:Iterable[Long],args2:Array[(Long, Long)]): Long = {
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

def tmp(args1:Iterable[Long],args2:Array[(Long, Long)]): List[(Long, Long)] = {
	var b = List[(Long, Long)]()
	for ( id <- args2){ 
		var c = 0
		for ( a <- args1){
			if( a == id._1 ){
				c += 1
			}	
		}
		for ( a <- args1){
			if( a == id._2 ){
				c += 1
			}	
		}
		if( c == 2){
			b::= id
		}
	}
	b
}

val vertexArray = Array((0L),(1L),(2L),(3L),(4L),(5L),(6L),(7L))
val vertices = sc.parallelize(vertexArray)

var idVertices = vertices.map(x => (x,x)).collect
// broadcast idVertices
var bIdVertices = sc.broadcast(idVertices)

val edgeArray = Array((0L,1L),(0L,2L),(1L,2L),(2L,3L),(3L,6L),(3L,4L),(4L,5L),(5L,6L),(6L,7L),(7L,5L),(7L,3L))

val edges = sc.parallelize(edgeArray)

val neighbor = edges.flatMap(x => Array((x._2,x._1),(x._1,x._2))).groupByKey()


for (i <- 1 to 20){
val tmp = neighbor.map( x => (x._1,returnNewId(x._2, bIdVertices.value))).collect()
idVertices = tmp
bIdVertices = sc.broadcast(idVertices)
}

val dCluster = sc.parallelize(bIdVertices.value)

val t1 = dCluster.map(x => (x._2,x._1)).groupByKey()

val t2 = t1.map(x => (x._2,tmp(x._2,edgeArray)))

