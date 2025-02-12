package controllers

import scala.jdk.CollectionConverters._
import com.gu.pandomainauth.PublicSettings
import model.{Check, MatcherResponse}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.MatcherPool
import com.gu.typerighter.lib.PandaAuthentication

import scala.concurrent.{ExecutionContext, Future}
import utils.Timer
import net.logstash.logback.marker.Markers

/**
  * The controller that handles API requests.
  */
class ApiController(
  cc: ControllerComponents,
  matcherPool: MatcherPool,
  val publicSettings: PublicSettings
)(implicit ec: ExecutionContext) extends AbstractController(cc) with PandaAuthentication {

  def check: Action[JsValue] = ApiAuthAction.async(parse.json) { request =>
    request.body.validate[Check].asEither match {
      case Right(check) =>
        val checkMarkers = check.toMarker
        val userMarkers = Markers.appendEntries(Map("userEmail" -> request.user.email).asJava)
        checkMarkers.add(userMarkers)

        val eventuallyMatches = Timer.timeAsync("ApiController.check", checkMarkers) {
          matcherPool.check(check)
        }

        eventuallyMatches.map {
          case (categoryIds, matches) => {
            val response = MatcherResponse(
              matches = matches,
              blocks = check.blocks,
              categoryIds = categoryIds
            )
            Ok(Json.toJson(response))
          }
        } recover {
        case e: Exception =>
          InternalServerError(Json.obj("error" -> e.getMessage))
      }
      case Left(error) =>
        Future.successful(BadRequest(s"Invalid request: $error"))
    }
  }

  def getCurrentCategories: Action[AnyContent] = ApiAuthAction {
      Ok(Json.toJson(matcherPool.getCurrentCategories))
  }
}
