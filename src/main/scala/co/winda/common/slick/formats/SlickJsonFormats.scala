package co.winda.common.slick.formats

import java.time._
import spray.json._
import java.util.UUID
import java.time.format.DateTimeFormatter
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.{WKTReader, WKTWriter}

/**
* @author David Karigithu
* @since 08-01-2016
*/

object SlickJsonFormats {

  implicit object UUIDJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }

  implicit object LocalDateTimeJsonFormat extends JsonFormat[LocalDateTime] {
    def write(x: LocalDateTime) = JsString(x.toString)
    def read(value: JsValue) = value match {
      case JsString(x) => LocalDateTime.parse(x)
      case x => deserializationError("Expected LocalDateTime as JsString, but found " + x)
    }
  }

  implicit object LocalDateJsonFormat extends JsonFormat[LocalDate] {
    def write(x: LocalDate) = JsString(x.format(DateTimeFormatter.ISO_DATE))
    def read(value: JsValue) = value match {
      case JsString(x) => LocalDate.parse(x)
      case x => deserializationError(s"Expected LocalDate as ISO_DATE Format, but found $x")
    }
  }

  implicit object DurationJsonFormat extends JsonFormat[Duration] {
    def write(x: Duration) = JsNumber(x.toMillis)
    def read(value: JsValue) = value match {
      case JsNumber(x) => Duration.ofMillis(x.toLong)
      case x => deserializationError(s"Expected Duration as JsObject, but found $x")
    }
  }

  implicit object PeriodJsonFormat extends JsonFormat[Period] {
    def write(x: Period) = JsNumber(x.getDays)
    def read(value: JsValue) = value match {
      case JsNumber(x) => Period.ofDays(x.toInt)
      case x => deserializationError(s"Expected Period as JsObject, but found $x")
    }
  }
 
  implicit def GeometryJsonFormat[G <: Geometry] = new RootJsonFormat[G] {
    def write(x: G) = JsString(toWKT[G](x))
    def read(value: JsValue) = value match {
      case JsString(x) => fromWKT[G](x)
      case x => deserializationError("Expected Geometry as JsArray, but found " + x)
    }
  }

  /////////////////////////////////////////////////////////////////
  private val wktWriterHolder = new ThreadLocal[WKTWriter]
  private val wktReaderHolder = new ThreadLocal[WKTReader]
 
  private def toWKT[T <: Geometry](geom: T): String = {
    if (wktWriterHolder.get == null) wktWriterHolder.set(new WKTWriter())
    wktWriterHolder.get.write(geom)
  }
  private def fromWKT[T <: Geometry](wkt: String): T = {
    if (wktReaderHolder.get == null) wktReaderHolder.set(new WKTReader())
    wktReaderHolder.get.read(wkt).asInstanceOf[T]
  }
}