val r = scala.util.Random

val slength = 5
var matrix = Array.ofDim[Int](slength,slength)

for( i <- 0 to slength-1 ) {
  for( j <- 0 to slength-1 ) {
    matrix(i)(j) = r.nextInt(6)
  }
}

// val data = Array(Array(4, 4, 0, 2, 2),Array(4, 0, 2, 0, 3),Array(4, 0, 0, 1, 1),Array(1, 1, 1, 2, 0),Array(2, 2, 4, 0, 0),Array(5, 0, 5, 0, 2),Array(1, 1, 1, 3, 0),Array(0, 1, 3, 0, 1))
val parData = sc.parallelize(matrix)
  /*
   * This method takes 2 equal length arrays of integers 
   * It returns a double representing similarity of the 2 arrays
   * 0.9925 would be 99.25% similar
   * (x dot y)/||X|| ||Y||
   */
  def cosineSimilarity(x: Array[Int], y: Array[Int]): Double = {
    require(x.size == y.size)
    dotProduct(x, y)/(magnitude(x) * magnitude(y))
  }
  
  /*
   * Return the dot product of the 2 arrays
   * e.g. (a[0]*b[0])+(a[1]*a[2])
   */
  def dotProduct(x: Array[Int], y: Array[Int]): Int = {
    (for((a, b) <- x zip y) yield a * b) sum
  }
  
  /*
   * Return the magnitude of an array
   * We multiply each element, sum it, then square root the result.
   */
  def magnitude(x: Array[Int]): Double = {
    math.sqrt(x map(i => i*i) sum)
  }
  

val cartesianData = parData.cartesian(parData)
val userCosSimilarity = cartesianData.map( x => cosineSimilarity(x._1,x._2)).collect()


