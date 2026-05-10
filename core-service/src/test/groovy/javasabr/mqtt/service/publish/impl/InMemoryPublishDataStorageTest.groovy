package javasabr.mqtt.service.publish.impl

import java.nio.ByteBuffer
import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.test.support.UnitSpecification

import static java.nio.charset.StandardCharsets.UTF_8

class InMemoryPublishDataStorageTest extends UnitSpecification {

  def "should store publish data with all attributes and find it by id"() {
    given:
        def publishDataStorage = new InMemoryPublishDataStorage()
        def testDataId = UUID.randomUUID()
        def testContentType = "application/json"
        def testPayloadFormat = PayloadFormat.UTF8_STRING
        def testPayload = '{"key":"value"}'.getBytes(UTF_8)
        def testCorrelationData = 'correlation-data'.getBytes(UTF_8)
    when:
        def storedPublishData = publishDataStorage.store(
            testDataId,
            testContentType,
            testPayloadFormat,
            testPayload,
            testCorrelationData)
        def foundPublishData = publishDataStorage.findById(testDataId)
        def payloadBuffer = ByteBuffer.allocate(testPayload.length)
        def correlationDataBuffer = ByteBuffer.allocate(testCorrelationData.length)
        foundPublishData.writePayloadTo(payloadBuffer)
        foundPublishData.writeCorrelationDataTo(correlationDataBuffer)
    then:
        publishDataStorage.idToPublishData.size() == 1
        publishDataStorage.idToPublishData.containsKey(testDataId)
        storedPublishData.is(foundPublishData)
        with(foundPublishData) {
          id() == testDataId
          contentType() == testContentType
          payloadFormat() == testPayloadFormat
          payloadSize() == testPayload.length
          correlationDataSize() == testCorrelationData.length
          !isPayloadEmpty()
          !isCorrelationDataEmpty()
        }
        new String(payloadBuffer.array(), UTF_8) == '{"key":"value"}'
        new String(correlationDataBuffer.array(), UTF_8) == 'correlation-data'
  }

  def "should return null for missing publish data by id"() {
    given:
        def publishDataStorage = new InMemoryPublishDataStorage()
        def testUnknownId = UUID.randomUUID()
    expect:
        publishDataStorage.findById(testUnknownId) == null
  }

  def "should not allow to store publish data twice with the same id"() {
    given:
        def publishDataStorage = new InMemoryPublishDataStorage()
        def testDataId = UUID.randomUUID()
        def testPayload = "payload".getBytes(UTF_8)
        def testCorrelationData = "correlation".getBytes(UTF_8)
        publishDataStorage.store(
            testDataId,
            "text/plain",
            PayloadFormat.UTF8_STRING,
            testPayload,
            testCorrelationData)
    when:
        publishDataStorage.store(
            testDataId,
            "application/octet-stream",
            PayloadFormat.BINARY,
            "other-payload".getBytes(UTF_8),
            null)
    then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Publish data with id:[${testDataId}] already exists"
        publishDataStorage.idToPublishData.size() == 1
  }

  def "should remove stored publish data by id"() {
    given:
        def publishDataStorage = new InMemoryPublishDataStorage()
        def testDataId = UUID.randomUUID()
        publishDataStorage.store(
            testDataId,
            "text/plain",
            PayloadFormat.UTF8_STRING,
            "payload".getBytes(UTF_8),
            "correlation".getBytes(UTF_8))
    when:
        publishDataStorage.removeById(testDataId)
    then:
        publishDataStorage.idToPublishData.isEmpty()
        publishDataStorage.findById(testDataId) == null
  }

  def "should ignore removing missing publish data by id"() {
    given:
        def publishDataStorage = new InMemoryPublishDataStorage()
        def testUnknownId = UUID.randomUUID()
    when:
        publishDataStorage.removeById(testUnknownId)
    then:
        noExceptionThrown()
        publishDataStorage.idToPublishData.isEmpty()
  }
}
