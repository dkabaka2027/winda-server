package co.winda

import co.winda.enums._
import co.winda.models._
import co.winda.android._
import co.winda.common.security.models._
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import com.github.tminglei.slickpg.InetString
import co.winda.common.security.enums.Algorithm
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import co.winda.crawler.models.{Extractor, Field}
import com.vividsolutions.jts.geom.{Coordinate, Point, PrecisionModel}
import spray.json
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNumber, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat}

trait JsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object CountryJsonFormat extends RootJsonFormat[CountryType.Value] {

    override def write(obj: CountryType.Value) = JsString(obj.toString())

    override def read(json: JsValue): CountryType.Value = json match {
      case JsNumber(s) => CountryType(s.toInt)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object GameStatusJsonFormat extends RootJsonFormat[GameStatus.Value] {

    override def write(obj: GameStatus.Value) = JsString(obj.toString())

    override def read(json: JsValue): GameStatus.Value = json match {
      case JsNumber(s) => GameStatus(s.toInt)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object GameTypeJsonFormat extends RootJsonFormat[GameType.Value] {

    override def write(obj: GameType.Value) = JsString(obj.toString())

    override def read(json: JsValue): GameType.Value = json match {
      case JsNumber(s) => GameType(s.toInt)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object GameEventTypeJsonFormat extends RootJsonFormat[GameEventType.Value] {

    override def write(obj: GameEventType.Value) = JsString(obj.toString())

    override def read(json: JsValue): GameEventType.Value = json match {
      case JsNumber(s) => GameEventType(s.toInt)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object PositionTypeJsonFormat extends RootJsonFormat[PositionType.Value] {

    override def write(obj: PositionType.Value) = JsString(obj.toString())

    override def read(json: JsValue): PositionType.Value = json match {
      case JsNumber(s) => PositionType(s.toInt)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object AlgorithmJsonFormat extends RootJsonFormat[Algorithm.Value] {

    override def write(obj: Algorithm.Value) = JsString(obj.toString())

    override def read(json: JsValue): Algorithm.Value = json match {
      case JsNumber(s) => Algorithm(s.toInt)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object LocalDateJsonFormat extends RootJsonFormat[LocalDate] {

    private val parserISO : DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-mm-dd")

    override def write(obj: LocalDate) = JsString(parserISO.format(obj))

    override def read(json: JsValue) : LocalDate = json match {
      case JsString(s) => LocalDate.parse(s)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object LocalDateTimeJsonFormat extends RootJsonFormat[LocalDateTime] {

    private val parserISO : DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-mm-dd")

    override def write(obj: LocalDateTime) = JsString(parserISO.format(obj))

    override def read(json: JsValue) : LocalDateTime = json match {
      case JsString(s) => LocalDateTime.parse(s)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object PointJsonFormat extends RootJsonFormat[Point] {

    override def write(obj: Point) = JsArray(
      JsNumber(obj.getCoordinate.x),
      JsNumber(obj.getCoordinate.y),
      JsNumber(obj.getCoordinate.z)
    )

    override def read(json: JsValue): Point = json match {
      case JsArray(coordinates: Vector[JsValue]) => new Point(new Coordinate(coordinates(0).convertTo[Double], coordinates(1).convertTo[Double]), new PrecisionModel(PrecisionModel.FIXED), 3)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object InetJsonFormat extends RootJsonFormat[InetString] {

    override def write(obj: InetString) = JsString(obj.value)

    override def read(json: JsValue): InetString = json match {
      case JsString(coords) => InetString(coords)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit val country: RootJsonFormat[Country] = jsonFormat8(Country)
  implicit val game: RootJsonFormat[Game] = jsonFormat15(Game)
  implicit val gameEvent: RootJsonFormat[GameEvent] = jsonFormat7(GameEvent)
  implicit val gameStatistic: RootJsonFormat[GameStatistic] = jsonFormat22(GameStatistic)
  implicit val field: JsonFormat[Field] = lazyFormat(jsonFormat(Field, "key", "dataType", "selector", "fields"))
  implicit val extractor: RootJsonFormat[Extractor] = jsonFormat3(Extractor)
  implicit val job: RootJsonFormat[Job] = jsonFormat12(Job)
  implicit val league: RootJsonFormat[League] = jsonFormat9(League)
  implicit val method: RootJsonFormat[Method] = jsonFormat6(Method)
  implicit val newses: RootJsonFormat[News] = jsonFormat7(News)
  implicit val permission: RootJsonFormat[Permission] = jsonFormat10(Permission)
  implicit val player: RootJsonFormat[Player] = jsonFormat9(Player)
  implicit val playerSquad: RootJsonFormat[PlayerSquad] = jsonFormat7(PlayerSquad)
  implicit val playerStatistic: RootJsonFormat[PlayerStatistic] = jsonFormat10(PlayerStatistic)
  implicit val playerTeam: RootJsonFormat[PlayerTeam] = jsonFormat10(PlayerTeam)
  implicit val role: RootJsonFormat[Role] = jsonFormat7(Role)
  implicit val season: RootJsonFormat[Season] = jsonFormat7(Season)
  implicit val squad: RootJsonFormat[Squad] = jsonFormat5(Squad)
  implicit val stadium: RootJsonFormat[Stadium] = jsonFormat8(Stadium)
  implicit val subscription: RootJsonFormat[Subscription] = jsonFormat8(Subscription)
  implicit val team: RootJsonFormat[Team] = jsonFormat17(Team)
  implicit val token: RootJsonFormat[Token] = jsonFormat6(Token)
  implicit val transaction: RootJsonFormat[Transaction] = jsonFormat10(Transaction)
  implicit val user: RootJsonFormat[User] = jsonFormat14(User)

  // Generic
  implicit val confirm: RootJsonFormat[Confirm] = jsonFormat2(Confirm)
  implicit val credential: RootJsonFormat[Credential] = jsonFormat3(Credential)
  implicit val jwt: RootJsonFormat[JWT] = jsonFormat3(JWT)
  implicit val jwtToken: RootJsonFormat[JwtToken] = jsonFormat2(JwtToken)
  implicit val message: RootJsonFormat[Message] = jsonFormat2(Message)
  implicit val registration: RootJsonFormat[Registration] = jsonFormat7(Registration)
  implicit val reset: RootJsonFormat[Reset] = jsonFormat1(Reset)

  // Android
  implicit val homeResponse: RootJsonFormat[HomeResponse] = jsonFormat3(HomeResponse)
  implicit val playerResponse: RootJsonFormat[PlayerResponse] = jsonFormat2(PlayerResponse)
  implicit val head2head: RootJsonFormat[Head2Head] = jsonFormat3(Head2Head)
  implicit val leagueItem: RootJsonFormat[LeagueItem] = jsonFormat6(LeagueItem)
  implicit val gameResponse: RootJsonFormat[GameResponse] = jsonFormat11(GameResponse)
  implicit val gameItemResponse: RootJsonFormat[GameItemResponse] = jsonFormat4(GameItemResponse)
  implicit val resultResponse: RootJsonFormat[ResultResponse] = jsonFormat7(ResultResponse)
  implicit val leagueResponse: RootJsonFormat[LeagueResponse] = jsonFormat3(LeagueResponse)
  implicit val payment: RootJsonFormat[Payment] = jsonFormat9(Payment)

}
