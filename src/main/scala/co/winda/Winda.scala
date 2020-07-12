package co.winda

import akka.io.IO
import akka.pattern._
import akka.actor.{ActorContext, ActorSystem, Cancellable, Props}
import akka.io.Tcp.Bind
import akka.http.scaladsl.Http
import java.net.InetSocketAddress

import akka.stream.ActorMaterializer
import akka.util.Timeout
import co.winda.Tables.jobs

import scala.concurrent.duration._
import co.winda.common.modules.{ActorModule, Persistence}
import co.winda.common.slick.driver.PostgresGeoDriver
import co.winda.crawler.Crawler
import co.winda.models.Job
import co.winda.predictive.{Network, Trainer}
import co.winda.schedulers.{Authentication, AuthenticationInit, SubscriptionInit, Subscriptions}
import com.coiney.akka.mailer.MailerSystem
import com.redis.RedisClient
import com.typesafe.config.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, Await}
import scala.io.StdIn

/**
* @author David Karigithu
* @since 21-09-2016
*/
object Winda extends App {

    val module = new Persistence with ActorModule

    implicit val system: ActorSystem = module.system
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val mailer: MailerSystem = MailerSystem()
    implicit val config: Config = module.config
    implicit val ec: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout = 60.seconds
    implicit val db: PostgresGeoDriver#Backend#Database = module.db
    implicit val profile: PostgresGeoDriver = module.profile
    //implicit val redis: RedisClient = RedisClient("localhost", 6379)

    import profile.api._

    // Initialise Migrations
    Await.result(Migrations.run(db, profile, ec), Duration.Inf)
    
    Await.result(Seeder(db, profile, materializer, ec), Duration.Inf)


    // Initialise Crawler
    val js = Await.result(db.run(jobs.result.transactionally), Duration.Inf)
    val crawlers: Seq[Cancellable] = js.map(Crawler.run(_))

    // Initialise Network
    // TODO: Trainer should only begin after crawlers have setup Database
    // val trainer = new Trainer()
    // val network = new Network()

    // Initialise Schedulers
   // val authentication = system.scheduler.schedule(0.seconds, 15.minutes, system.actorOf(Props[Authentication](), "authentication"), AuthenticationInit)
   // val subscriptions = system.scheduler.schedule(0.seconds, 24.hours, system.actorOf(Props[Subscriptions](), "subscriptions"), SubscriptionInit)

    // The Router Actor for this Application
    val router = new API

    val future = Http().bindAndHandle(router.router, config.getString("app.interface"), config.getInt("app.port"))

    println(s"Server online at http://localhost:${config.getInt("app.port")}/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return

    future
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete { _ =>
            crawlers.map(c => c.cancel())
            db.close
            // authentication.cancel()
            // subscriptions.cancel()
            system.terminate()

        } // and shutdown when done

}