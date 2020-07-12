package co.winda.common.slick.extensions

import scala.annotation.tailrec
import scala.language.higherKinds
import co.winda.common.utils.Parsers._
import com.vividsolutions.jts.geom.{Geometry, Point}
import co.winda.common.slick.driver.PostgresGeoDriver.api._

/**
 * @author David Karigithu
 * @since 08-01-2016
 */

object QueryExtensions {

  implicit class FilterExtensions[E, T <: Table[E]](val q: Query[T, E, Seq]) {

    /**
    * Pages the Query using a page number and offset size
    * @param pages
    * @param pageSize
    * @return
    */
    def page(pages: Int, pageSize: Int): Query[T, E, Seq] = {
      q.drop((pages - 1) * pageSize).take(pageSize)
    }

    /**
    *
    * @param sortKeys
    * @return
    */
    def sort(sortKeys: Option[List[Seq[String]]]): Query[T, E, Seq] = {
      sortKeys match {
        case None => q
        case Some(sortKeys: List[Seq[String]]) =>
          sortKeys match {
            case key :: tail =>
              sort(Some(tail)).sortBy(table =>
                key match {
                  case name :: Nil => table.column[String](name).asc
                  case name :: "asc" :: Nil => table.column[String](name).asc
                  case name :: "desc" :: Nil => table.column[String](name).desc
                  case o => throw new Exception("invalid sorting key: " + o)
                }
              )
            case Nil => q
          }
      }
    }

    /**
    *
    * @param refine
    * @return
    */
    def refine(refine: Option[String]): Query[T, E, Seq] = {
      refineParser(refine) match {
        case None => q
        case Some(refineKeys: List[(String, String, String, Option[String])]) =>
          q.filter(
            table => {
              refineKeys.foldLeft(
                LiteralColumn(true): Rep[Boolean]
              ) {
                case (condition, (name, op, value, None)) => condition && ((op match {
                  case "=" => table.column[String](name) === LiteralColumn[String](value)
                  case "not"  => table.column[String](name) =!= LiteralColumn[String](value)
                  case "gt" => table.column[String](name) > LiteralColumn[String](value)
                  case "lt" => table.column[String](name) < LiteralColumn[String](value)
                  case "gte" => table.column[String](name) >= LiteralColumn[String](value)
                  case "lte" => table.column[String](name) <= LiteralColumn[String](value)
                  case "~" => table.column[String](name) like ("%" + LiteralColumn(value) + "%")
                  case o => throw new Exception("invalid filter parameters: " + o)
                }): Rep[Boolean])
//                case (condition, (name, op, value, comparative)) => condition && ((op match {
//                  case "within" => table.column[Geometry](name) <-> value.asColumnOf[Double])
//                  case "near" => table.column[Geometry](name).>=(value.bind)
//                }): Rep[Boolean])
              }: Rep[Boolean]
            }
          )
      }
    }

  }

}

