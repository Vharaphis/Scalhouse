import zio.*
import zio.console.Console
import zio.http.*
import zio.json.*
import zio.stream.*
import java.net.URL

case class TestTime(time: String)
case class TestTemp(temperature_2m_max: Double)
case class TemperatureOfTheDay(time: String, temperature_2m_max: Double)

object NewMain extends ZIOAppDefault {
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
                        t_time <- body.fromJson[TestTime]
                        t_temp <- body.fromJson[TestTemp]
                        zipped = ZStream((t_time, t_temp))
                            .foreach(
                                (t_time, t_temp) => ZIO.succeed(TemperatureOfTheDay(t_time.time, t_temp.temperature_2m_max))
                            )
                    } yield zipped
                }
                .foreach(x => Console.printLine(x))
        } yield ()

        appLogic.provide(Client.default)
    }
}