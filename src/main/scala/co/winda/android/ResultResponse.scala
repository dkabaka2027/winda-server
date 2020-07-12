package co.winda.android

import co.winda.models.Team

case class ResultResponse(homeTeam: Team, awayTeam: Team, homeScore: Int, awayScore: Int, homePrediction: Double,
                          drawPrediction: Double, awayPrediction: Double)
