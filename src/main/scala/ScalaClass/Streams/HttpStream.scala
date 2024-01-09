// import zio._
// import zio.Console._
// import zio.http._
// import zio.stream._
// import zio.Duration._

// object HttpStream {

//   def fetchData() = {
//     val url = URL
//       .decode(
//         // appel de l'api open-meteo
//         "https://api.open-meteo.com/v1/forecast?latitude=43.7392&longitude=0.0087&current=temperature_2m,is_day,precipitation,rain,showers,cloud_cover,wind_speed_10m&timezone=Europe%2FBerlin"
//       )
//       .toOption
//       .get // unsafe

//     for {
//       client <- ZIO.service[Client]
//       res <- client.url(url).get("/")
//       body <- res.toString
//     } yield body
//   }
// }
