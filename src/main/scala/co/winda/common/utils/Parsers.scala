package co.winda.common.utils

/**
 * @author David Karigithu
 * @since 08-01-2016
 */

object Parsers {

  /**
   * Parse Sort String
   *
   * @param sortString = "[name:sort,name:sort]"
   * @return List[List[String]]
   */

  def sortParser(sortString: Option[String]): Option[List[Seq[String]]] = {
    sortString match {
      case None => None
      case Some(ss: String) => 
        Some(ss.stripPrefix("[").stripSuffix("]").split(',').toList.map(_.split(':').toList))
    }
  }

  /**
   * Parse Refine String
   *
   * @param refineString "[model:op:value,model:op:value]"
   * @return List[List[String]]
   */

  def refineParser(refineString: Option[String]): Option[List[(String, String, String, Option[String])]] = {
    val Extract = "([a-zA-Z_*]):([=|not|gt|lt|gte|lte|~|near|within]):([a-zA-Z_]*):([a-zA-Z_]*?)".r

    refineString match {
      case None =>
        None
      case Some(rs: String) => 
        Some(rs.stripPrefix("[").stripSuffix("]").split(",").toList.map {
          case Extract(name, op, value, comparator) => (name, op, value, Option(comparator))
        })
    }
  
  }

  /**
   * Starcom EAI Parser
   *
   * @param packet Model(field, field, field)
   * @return Map[String, String]
   */
  def eaiParser(packet: String): Map[String, String] = {
    packet.stripSuffix("|").stripPrefix("|").split(",").map { i =>
      val a = i.split("=")
      (a.head, a.tail.head)
    }.toMap[String, String]
  }

}
