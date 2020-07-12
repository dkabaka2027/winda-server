package co.winda.crawler.models

case class Field(key: String, dataType: String, selector: Option[String], fields: List[Field] = List.empty[Field])
