package controllers

import akka.stream.scaladsl.{Flow, Sink, Source}
import scala.collection.JavaConverters._
import com.gu.pandomainauth.PublicSettings
import model.{Check, CheckResponse}
import actor.{WsCheckActor}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.{PandaAuthentication, MatcherPool}

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.streams.ActorFlow
import utils.Timer
import net.logstash.logback.marker.Markers
import akka.stream.Materializer
import akka.actor.ActorSystem

/**
  * The controller that handles API requests.
  */
class ApiController(
  cc: ControllerComponents,
  matcherPool: MatcherPool,
  val publicSettings: PublicSettings
)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem) extends AbstractController(cc) with PandaAuthentication {

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
            val response = CheckResponse(
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

  def checkStream = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    ApiAuthAction.toSocket(request) { (u, r) => ActorFlow.actorRef { out =>
        WsCheckActor.props(out, matcherPool)
      }
    }
  }

  def getCurrentCategories: Action[AnyContent] = ApiAuthAction { request =>
    Ok(Json.toJson(matcherPool.getCurrentCategories))
  }
}
