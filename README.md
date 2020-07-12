# Winda Server
Predictive analytics for Soccer using Deeplearning and Statistical Probability. Features:
+ A web crawler for scrapping games, player statistics and events data to seed the database;
+ A deeplearning deterministic inference engine using DeepLearning4j;
+ A API-first design using REST and WebSockets for realtime updates;
+ A web interface built with React.

Powered by Scala with Akka Actors, Clustering, HTTP and Streams to provide a reactive real-time server that allows for dynamic subscriptions.

## Install
Ensure you have Scala, SBT (Simple Build Tool) & PostgreSQL installed.

Create a user role `winda` with password `winda` & database with previously create role `winda` in Postgres.

Download ChromeDriver from [here](https://chromedriver.chromium.org/downloads) and extract the executable and place it in a folder of your choice. Edit `src/main/scala/co/winda/crawler/Crawler.scala` on line 61 with location of the ChromeDriver executable.

Git clone this repository and `cd` into directory and execute `sbt run`.

## Usage

## Development

## Bugs
+ There is currently a bug that prevents ChromeDriver from opening URLs due to some session issues, currently ironing it out, if it persists will move over to PhantomJS.