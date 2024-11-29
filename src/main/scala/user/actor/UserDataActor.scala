package org.cs441homework2.com
package user.actor

import akka.actor.Actor
import org.cs441homework2.com.user.data.UserData

object UserDataActor {
  case object Get
  case object Post
  case object Put
  case object Delete
}

//Backend Service
class UserDataActor extends Actor {

  import UserDataActor._

  override def receive: Receive = {

    case Get =>
      sender() ! UserData("data Searched")
    case Post =>
      sender() ! UserData("data created!")
    case Put =>
      sender () ! UserData("data updated!")
    case Delete =>
      sender () ! UserData("data deleted!")
  }
}