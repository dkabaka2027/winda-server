package co.winda.common.security.enums

/**
* @author David Karigithu
* @since 26-09-2016
*/
class Algorithm extends Enumeration {
  val MD5, SHA256, SHA512 = Value
}

object Algorithm extends Algorithm