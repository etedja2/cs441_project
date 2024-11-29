package org.cs441homework2.com

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.PathDirectives.pathPrefix
import akka.stream.ActorMaterializer
import org.cs441homework2.com.user.actor.UserDataActor
import org.cs441homework2.com.user.repositories.UserActivityRepositoryImpl

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer extends App {
  implicit val system: ActorSystem = ActorSystem("web-app")
  private implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  private implicit val materialize: ActorMaterializer = ActorMaterializer()

  implicit val userActivityRepo = new UserActivityRepositoryImpl()
  implicit val userDataActorRef: ActorRef = system.actorOf(Props(new UserDataActor()))

  private val routeConfig = new RouteConfig()
  val routes = {
    pathPrefix("api") {
      concat(
        routeConfig.getRoute,
        routeConfig.postRoute,
        routeConfig.deleteRoute,
        routeConfig.putRoute
      )
    }
  }
  val serverFuture = Http().newServerAt("localhost", 8080).bind(routes)
  println("Server starting at http://localhost:8080")

  println("Server started ...")
  StdIn.readLine()
  serverFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}