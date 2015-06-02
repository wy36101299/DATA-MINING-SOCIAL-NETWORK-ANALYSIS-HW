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
val data = sc.textFile("/Users/wy/Desktop/bigfb2.txt")
val edgeRdd =  data.map(s => ( s.split(",")(0) , s.split(",")(1) ))
val vertexRdd = data.flatMap(s => s.split(",")).distinct()

// broadcast idVertices
var idVertices = vertexRdd.map(x => (x,x)).collect
var bIdVertices = sc.broadcast(idVertices)

val neighborRdd = edgeRdd.flatMap(x => Array( (x._2,x._1) ,(x._1,x._2) )).groupByKey()

// (id,dcluster)
for (i <- 1 to 20){
val newIdVertices = neighborRdd.map( x => (x._1,updateNewId(x._2, bIdVertices.value))).collect()
bIdVertices = sc.broadcast(newIdVertices)
}

val dCluster = sc.parallelize(bIdVertices.value)

val dVerCluster = dCluster.map(x => (x._2,x._1)).groupByKey()

val edgeArray = edgeRdd.collect()

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

val dvertexEdges = dVerCluster.map(x => (x._2,dSameVertexEdges(x._2, edgeArray)))






