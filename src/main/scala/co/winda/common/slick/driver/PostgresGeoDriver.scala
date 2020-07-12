package co.winda.common.slick.driver

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import co.winda.common.security.enums.Algorithm
import co.winda.crawler.models.Extractor
import co.winda.enums._
import spray.json._
//import DefaultJsonProtocol._
import co.winda.JsonProtocol
import slick.basic.Capability
import com.github.tminglei.slickpg._

/**
* @author David Karigithu
* @since 21-09-2016
*/
trait PostgresGeoDriver extends ExPostgresProfile
  with PgArraySupport
  with PgDateSupport
  with PgRangeSupport
  with PgHStoreSupport
  with PgSprayJsonSupport
  with PgSearchSupport
  with PgPostGISSupport
  with PgNetSupport
  with PgLTreeSupport
  with JsonProtocol {

  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
  super.computeCapabilities + Capability("insertOrUpdate")

  override val api = API

  object API extends API with ArrayImplicits
    with DateTimeImplicits
    with JsonImplicits
    with NetImplicits
    with PostGISImplicits
    with LTreeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val playJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JsValue](pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JsValue](pimpString(_).parseJson)(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
      ).to(_.toList)
    implicit val positionTypeMappedJdbcType = MappedColumnType.base[PositionType.Value, Int](
      (pt) => pt.id,
      (i) => PositionType(i)
    )
    implicit val countryTypeMappedJdbcType = MappedColumnType.base[CountryType.Value, Int](
      (ct) => ct.id,
      (i) => CountryType(i)
    )
    implicit val gameEventTypeMappedJdbcType = MappedColumnType.base[GameEventType.Value, Int](
      (get) => get.id,
      (i) => GameEventType(i)
    )
    implicit val gameTypeMappedJdbcType = MappedColumnType.base[GameType.Value, Int](
      (gt) => gt.id,
      (i) => GameType(i)
    )
    implicit val gameStatusMappedJdbcType = MappedColumnType.base[GameStatus.Value, Int](
      (gt) => gt.id,
      (i) => GameStatus(i)
    )
    implicit val algorithmMappedJdbcType = MappedColumnType.base[Algorithm.Value, Int](
      (gt) => gt.id,
      (i) => Algorithm(i)
    )
    implicit val extractorMappedJdbcType = MappedColumnType.base[Extractor, String](
      (e: Extractor) => e.toJson.prettyPrint,
      (s: String) => JsonParser(ParserInput(s)).convertTo[Extractor]
    )
    implicit val extractorListMappedJdbcType = MappedColumnType.base[List[Extractor], String](
      (e: List[Extractor]) => e.toJson.prettyPrint,
      (s: String) => JsonParser(ParserInput(s)).convertTo[List[Extractor]]
    )
  }
}

object PostgresGeoDriver extends PostgresGeoDriver