package co.winda.models

import java.time.{LocalDate, LocalDateTime}
import co.winda.common.slick.base.BaseEntity

/**
* @author David Karigithu
* @since 14/06/16
*/
case class PlayerTeam(id: Option[Long], buyingPrice: Long, weeklySalary: Long, from: LocalDate, to: Option[LocalDate],
                      created: Option[LocalDateTime], modified: Option[LocalDateTime], currentStat: Long, playerId: Long,
                      teamId: Long) extends BaseEntity[Long]