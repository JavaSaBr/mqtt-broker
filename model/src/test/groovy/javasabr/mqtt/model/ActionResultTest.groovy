package javasabr.mqtt.model

import javasabr.mqtt.test.support.UnitSpecification
import spock.lang.Unroll

import static javasabr.mqtt.model.ActionResult.*

class ActionResultTest extends UnitSpecification {

  @Unroll
  def "#first and #second == #result"(
      ActionResult first,
      ActionResult second,
      ActionResult result) {
    expect:
        first.and(second) == result
    where:
        first   | second  | result
        SUCCESS | SUCCESS | SUCCESS
        SUCCESS | FAILED  | FAILED
        FAILED  | SUCCESS | FAILED
        FAILED  | FAILED  | FAILED
        EMPTY   | EMPTY   | EMPTY
        EMPTY   | FAILED  | FAILED
        FAILED  | EMPTY   | FAILED
        SUCCESS | EMPTY   | SUCCESS
        EMPTY   | SUCCESS | SUCCESS
  }

}
