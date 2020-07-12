package co.winda.android

import co.winda.models.Team

case class Standing(team: Team, played: Int, won: Int, drawn: Int, lost: Int, point: Int)
