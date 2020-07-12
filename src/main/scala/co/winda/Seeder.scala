package co.winda

import spray.json.DefaultJsonProtocol
import spray.json._
import DefaultJsonProtocol._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import co.winda.Tables._
import co.winda.common.slick.base.BaseTable
import co.winda.common.slick.driver.PostgresGeoDriver
import co.winda.common.slick.driver.PostgresGeoDriver.api._
import co.winda.models._
import co.winda.tables._
import scala.concurrent.ExecutionContext
import scala.io.Source

object Seeder extends JsonProtocol {

  def apply(implicit db: PostgresGeoDriver#Backend#Database, profile: PostgresGeoDriver, materializer: ActorMaterializer, ec: ExecutionContext) = {
    val files: Array[String] = Array(
      "/countries.json",
      "/roles.json",
      "/users.json",
      "/jobs.json",
      "/methods.json",
      "/subscriptions.json",
      "/stadiums.json"
    ).map { f =>
      // println(f)
      val source = Source.fromURL(getClass.getResource(f))
      try source.mkString finally source.close
    }

    val cs = files.head.parseJson.convertTo[Seq[Country]]
    val rs = files(1).parseJson.convertTo[Seq[Role]]
    val us = files(2).parseJson.convertTo[Seq[User]]
    val js = files(3).parseJson.convertTo[Seq[Job]]
    val ms = files(4).parseJson.convertTo[Seq[Method]]
    val subs = files(5).parseJson.convertTo[Seq[Subscription]]
    // val stas = files(6).parseJson.convertTo[Seq[Stadium]]

    db.run(
      DBIO.seq(
        countries ++= cs,
        roles ++= rs,
        users ++= us,
        jobs ++= js,
        methods ++= ms,
        subscriptions ++= subs
        // stadiums ++= stas,
      )
    )
  }

}
