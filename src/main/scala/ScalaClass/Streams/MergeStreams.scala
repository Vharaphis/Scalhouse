// import zio._
// import zio.stream._
// import HttpStream._
// import SimpleStream._

// object MergeStreams {

//   def mergeStreams() = {

//     val s1 = HttpStream.fetchData()
//     val s2 = SimpleStream.createData()

//     val merged = ZStream
//       .mergeAllUnbounded(16)(s1, s2)
//       .take(50)
//       .grouped(5)
//       .foreach(chunk => Console.printLine(chunk.foldLeft(0)((a, b) => a + b)))
//   }
// }
