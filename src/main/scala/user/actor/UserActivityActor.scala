package org.cs441homework2.com
package user.actor

import akka.actor.Actor
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.pattern.pipe
import org.cs441homework2.com.user.repositories.UserActivityRepository

import scala.concurrent.ExecutionContextExecutor

object UserActivityActor {
  case object Get
}

class UserActivityActor(val userId: String,
                        implicit val repository: UserActivityRepository)
  extends Actor {

  implicit val ec: ExecutionContextExecutor = context.dispatcher


  override def receive: Receive = {
    case Get =>
      repository.queryHistoricalActivities(userId) pipeTo sender
  }
}