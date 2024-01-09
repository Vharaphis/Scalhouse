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