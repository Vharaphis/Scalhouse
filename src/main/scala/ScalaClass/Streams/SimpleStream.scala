// import zio._
// import zio.stream._

// object SimpleStream {
//   def createData() = {
//     val run: stream.ZStream[Any, Nothing, Unit] =
//         for {
//           number <- ZStream
//             .fromIterable((1 to 10).toList)
//             .take(5)
//             .map(_.toString)
//         } yield number
//   }
// }