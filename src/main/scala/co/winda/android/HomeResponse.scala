package co.winda.android

import co.winda.models.{Game, News}

case class HomeResponse(news: Seq[News], games: Seq[Game], results: Seq[ResultResponse])
