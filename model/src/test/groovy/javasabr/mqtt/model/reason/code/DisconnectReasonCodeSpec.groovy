package javasabr.mqtt.model.reason.code

import javasabr.mqtt.test.support.UnitSpecification
import spock.lang.Unroll

class DisconnectReasonCodeSpec extends UnitSpecification {

  @Unroll
  def "ofCode resolves declared constant #constant by its number"() {
    expect:
    DisconnectReasonCode.ofCode(constant.number()) == constant

    where:
    constant << DisconnectReasonCode.values()
  }

  @Unroll
  def "ofCode throws IllegalArgumentException for sparse-gap or out-of-range number #number"() {
    when:
    DisconnectReasonCode.ofCode(number)

    then:
    thrown(IllegalArgumentException)

    where:
    number << [0x01, 0x02, 0x03, 0x05, 0x7F, 0xFF]
  }
}
