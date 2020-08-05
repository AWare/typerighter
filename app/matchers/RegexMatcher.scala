package matchers

import model.{RegexRule, RuleMatch}
import services.MatcherRequest
import utils.Matcher

import scala.concurrent.{ExecutionContext, Future}


/**
  * A Matcher for rules based on regular expressions.
  */
class RegexMatcher(category: String, rules: List[RegexRule]) extends Matcher {
  def getId() = "regex-validator"

  override def check(request: MatcherRequest)(implicit ec: ExecutionContext): Future[List[RuleMatch]] = {
    Future {
      rules.flatMap {
        checkRule(request, _)
      }
    }
  }

  override def getRules(): List[RegexRule] = rules

  override def getCategory(): String = category

  private def checkRule(request: MatcherRequest, rule: RegexRule): List[RuleMatch] = {
    request.blocks.flatMap { block =>
        rule.regex.findAllMatchIn(block.text).map { currentMatch => RuleMatch.fromMatch(currentMatch.start, currentMatch.end, block, rule) }
    }
  }
}
