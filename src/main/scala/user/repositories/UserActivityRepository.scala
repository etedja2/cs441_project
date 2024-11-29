package org.cs441homework2.com
package user.repositories

import org.cs441homework2.com.user.data.UserActivity
import scala.concurrent.Future

trait UserActivityRepository {
  def queryHistoricalActivities(userId: String):
  Future[List[UserActivity]]
}