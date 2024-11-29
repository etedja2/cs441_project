package org.cs441homework2.com

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives.{as, complete, delete, entity, get, path, post, put}
import akka.http.scaladsl.server.directives.{PathDirectives, RouteDirectives}
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import akka.pattern.Patterns
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model._
import com.typesafe.config.ConfigFactory
import org.cs441homework2.com.user.actor.{UserActivityActor, UserDataActor}
import org.cs441homework2.com.user.data.{UserActivity, UserData}
import org.cs441homework2.com.user.repositories.UserActivityRepositoryImpl
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

// Create a case class to store the received user query
case class UserQuery(query: String)

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  // Convert the UserQuery class to support JSON format of 1 parameter
  implicit val userQueryFormat: RootJsonFormat[UserQuery] = jsonFormat1(UserQuery)
}

class RouteConfig(implicit val userDataActorRef: ActorRef, implicit val system: ActorSystem) extends JsonSupport {
  val timeoutMills: Long = 2 * 1000

  val getRoute: Route =

    PathDirectives.pathPrefix("user"){
      path("activity") {
        get {

          val userData = findData(UserDataActor.Get)

          val userActivityActorRef: ActorRef =
            system.actorOf(Props(new UserActivityActor(userData.data, new UserActivityRepositoryImpl())))

          val activity: UserActivity = findUserActivityData(userActivityActorRef)
          RouteDirectives.complete(HttpEntity(activity.toString))
        }
      }
    }

  private def findUserActivityData(userActivityActorRef: ActorRef) = {
    val resultFuture = Patterns.ask(userActivityActorRef, UserActivityActor.Get, timeoutMills)
    val result: List[UserActivity] = Await.result(resultFuture, Duration.create(2, TimeUnit.SECONDS)).asInstanceOf[List[UserActivity]]
    val activity: UserActivity = result.head
    activity
  }

  val postRoute: Route = path("user") {
    post {
      // Use entity to retrieve the POST parameters.
      // as[CASE_CLASS] is used to convert case class into the proper format accepted by akka.
      // IMPORTANT: inside case class, make sure there is an implicit val, or else an error will occur.
      entity(as[UserQuery]) { userQuery =>
        val config = ConfigFactory.load()
        val outputPath = config.getString("output.outputPath")

        val generatedSentence = GenerateSentence.startingPoint(outputPath, userQuery.query)

        complete(generatedSentence)
      }
    }
  }

  val deleteRoute: Route = path("user") {
    delete {
      //TODO: DO SOME OPERATION TO DELETE USER DATA
      executeActorAndSearchData(UserDataActor.Delete)
    }
  }

  val putRoute: Route = path("user") {
    put {
      //TODO: DO SOME OPERATION TO UPDATE USER DATA
      executeActorAndSearchData(UserDataActor.Put)
    }
  }

  val executeActorAndSearchData: Any => StandardRoute = (message: Any) => {
    val result: UserData = findData(message)
    RouteDirectives.complete(HttpEntity(result.data))
  }

  private def findData(message: Any) = {
    val resultFuture = Patterns.ask(userDataActorRef, message, timeoutMillis = timeoutMills)
    val result: UserData = Await.result(resultFuture, Duration.create(2, TimeUnit.SECONDS)).asInstanceOf[UserData]
    result
  }
}