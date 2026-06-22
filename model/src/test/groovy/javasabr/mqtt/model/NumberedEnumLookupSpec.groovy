package javasabr.mqtt.model

import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.common.util.NumberedEnum
import spock.lang.Unroll

class NumberedEnumLookupSpec extends UnitSpecification {

  enum TestNumbered implements NumberedEnum<TestNumbered> {
    ZERO(0), TWO(2), FIVE(5)

    private final int code

    TestNumbered(int code) {
      this.code = code
    }

    @Override
    int number() {
      return code
    }
  }

  private NumberedEnumLookup<TestNumbered> lookup = new NumberedEnumLookup<>(TestNumbered.values())

  def "resolve returns the constant for a present number"() {
    expect:
    lookup.resolve(0) == TestNumbered.ZERO
    lookup.resolve(2) == TestNumbered.TWO
    lookup.resolve(5) == TestNumbered.FIVE
  }

  def "resolve returns null for a sparse gap between allocated codes"() {
    expect:
    lookup.resolve(1) == null
    lookup.resolve(3) == null
    lookup.resolve(4) == null
  }

  @Unroll
  def "resolve returns null for out-of-range number #number"() {
    expect:
    lookup.resolve(number) == null

    where:
    number << [-1, 6, 99]
  }

  def "resolve with default returns the constant when present"() {
    expect:
    lookup.resolve(2, TestNumbered.ZERO) == TestNumbered.TWO
  }

  @Unroll
  def "resolve with default returns default for missing number #number"() {
    expect:
    lookup.resolve(number, TestNumbered.ZERO) == TestNumbered.ZERO

    where:
    number << [-1, 1, 6, 99]
  }

  @Unroll
  def "require throws IllegalArgumentException for missing number #number"() {
    when:
    lookup.require(number)

    then:
    IllegalArgumentException exception = thrown()
    exception.message.contains("Unknown enum constant for number:")

    where:
    number << [-1, 1, 6, 99]
  }

  def "require returns the constant when present"() {
    expect:
    lookup.require(5) == TestNumbered.FIVE
  }

  def "table is sized to the maximum declared number plus one"() {
    expect:
    lookup.resolve(5) == TestNumbered.FIVE
    lookup.resolve(6) == null
  }
}
