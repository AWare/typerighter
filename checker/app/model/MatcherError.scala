package model

import play.api.libs.json._

case class MatcherError(error: String, id: Option[String] = None) {
  val `type` = "MATCHER_ERROR"
}

object MatcherError {
  implicit val writes = new Writes[MatcherError] {
    def writes(response: MatcherError) = Json.obj(
      "type" -> response.`type`,
      "id" -> response.id,
      "error" -> response.error
    )
  }
}
