// import zio._
// import zio.Console._
// import zio.Duration._
// import zio.http._
// import zio.stream._
// import MergeStreams._

// object Main extends ZIOAppDefault {
  
//   override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
//     val appLogic = for {
//       _ <- ZStream(MergeStreams.mergeStreams())
//         .repeat(Schedule.spaced(30.seconds))
//         .mapZIO { z =>
//           for {
//             res <- z
//             body <- res.body.asString
//             _ <- Console.printLine(s"body size is: ${body.length}")
//           } yield body
//         }
//         .foreach(Console.printLine(_))
//     } yield ()
  
//     appLogic.provide(Client.default, Scope.default)
//   }

// }

import zio.*
import zio.http.*
import zio.json.*
import zio.stream.*
import zio.json._

import java.time.LocalDate

case class TestTime(time: String)
case class TestTemp(temperature_2m_max: Double)
case class TemperatureOfTheDay(time: String, temperature_2m_max: Double)

object TestTime {
  implicit val decoder: JsonDecoder[TestTime] = DeriveJsonDecoder.gen[TestTime]
}

object TestTemp {
  implicit val decoder: JsonDecoder[TestTemp] = DeriveJsonDecoder.gen[TestTemp]
}

object Main extends ZIOAppDefault {
    def fetchData() = {
        Console.printLine("Hereee")
        val latitude: String = "48.7167"
        val longitude: String = "2.25"

        val url: URL = URL.decode(s"https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&daily=temperature_2m_max,temperature_2m_min").toOption.get
        for {
            client <- ZIO.service[Client]
            res <- client.url(url).get("/")
        } yield (res)
    }

    override def run = {
        val appLogic = for {
            _ <- ZStream(fetchData())
                .mapZIO { z =>
                    for {
                        res <- z
                        body <- res.body.asString
                        t_timeEither <- body.fromJson[TestTime]
                        t_tempEither <- body.fromJson[TestTemp]
                        zipped <- (t_timeEither, t_tempEither) match {
                            case (Right(t_time), Right(t_temp)) =>
                                ZStream((t_time, t_temp))
                                    .foreach { case (t_time: TestTime, t_temp: TestTemp) => ZIO.succeed(TemperatureOfTheDay(t_time.time, t_temp.temperature_2m_max)) }
                            case (Left(error), _) =>
                                ZIO.fail(s"Failed to parse TestTime: $error")
                            case (_, Left(error)) =>
                                ZIO.fail(s"Failed to parse TestTemp: $error")
                        }
                        zipped <- ZIO.fromEither(zipped).flatMap(result => ZIO.succeed(Right(result))) // Convert Unit to Either
                    } yield zipped
                }
                .foreach(x => Console.printLine(x))
        } yield ()
    }
}
