package javasabr.mqtt.application.model

import NetworkUnitSpecification
import javasabr.mqtt.model.ActionResult
import spock.lang.Unroll

import static javasabr.mqtt.model.ActionResult.*

class ActionResultTest extends NetworkUnitSpecification {

  @Unroll
  def "#first and #second == #result"(
      ActionResult first,
      ActionResult second,
      ActionResult result
  ) {
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
