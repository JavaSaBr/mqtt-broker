package com.ss.mqtt.broker.test.model

import javasabr.mqtt.legacy.model.ActionResult
import com.ss.mqtt.broker.test.network.NetworkUnitSpecification
import spock.lang.Unroll

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
