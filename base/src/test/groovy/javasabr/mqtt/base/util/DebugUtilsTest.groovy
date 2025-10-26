package javasabr.mqtt.base.util

import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class DebugUtilsTest extends UnitSpecification {

  class TestData {
    String name = "testData"
    String ignored = "ignored"
    MutableArray<String> mutableValues = ArrayFactory.mutableArray(String)
    Array<String> values = ArrayFactory.mutableArray(String)
    Iterable<String> emptyArray = Array.empty(String)

    TestData() {
      def array = ArrayFactory.mutableArray(String)
      array.add("First")
      array.add("Second")
      this.mutableValues = array;
      this.values = Array.copyOf(array)
    }

    static {
      DebugUtils.registerIncludedFields(TestData,
          "name", "mutableValues", "values", "emptyArray")
    }

    String toString() {
      return DebugUtils.toJsonString(this)
    }
  }

  def "should correctly write class to json"() {
    given:
        def data = new TestData()
    when:
        def json = data.toString()
    then:
        json == """{
  "emptyArray" : [ ],
  "mutableValues" : [ "First", "Second" ],
  "name" : "testData",
  "values" : [ "First", "Second" ]
}"""
  }
}
