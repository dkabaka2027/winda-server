package co.winda.common.slick.base

import slick.lifted.Rep
import java.time.LocalDateTime


/**
* @author David Karigithu
* @since 21-09-2016
*/
trait BaseTable[I] {
  def id: Rep[I]

}