package javasabr.mqtt.model

import javasabr.mqtt.test.support.UnitSpecification
import spock.lang.Unroll

class QoSSpec extends UnitSpecification {

  @Unroll
  def "ofCode returns #expected for valid level #level"() {
    expect:
    QoS.ofCode(level) == expected

    where:
    level | expected
    0     | QoS.AT_MOST_ONCE
    1     | QoS.AT_LEAST_ONCE
    2     | QoS.EXACTLY_ONCE
  }

  @Unroll
  def "ofCode returns INVALID for level #level"() {
    expect:
    QoS.ofCode(level) == QoS.INVALID

    where:
    level << [3, 4, 9, -1]
  }
}
