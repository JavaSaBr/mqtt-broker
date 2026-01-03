package javasabr.mqtt.service.session.impl

import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.network.session.ConfigurableNetworkMqttSession
import javasabr.mqtt.service.IntegrationServiceSpecification

import java.time.Duration

class InMemoryMqttSessionServiceTest extends IntegrationServiceSpecification {
  
  InMemoryMqttSessionService sessionService = new InMemoryMqttSessionService(60_000)
 
  def "should create a fresh session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_1"
    when:
        def freshSession = sessionService.createClean(testClientId).block()
    then:
        freshSession != null
        with(freshSession) {
          clientId() == testClientId
          topicNameMapping().size() == 0
          activeSubscriptions().subscriptions().isEmpty()
          inProcessingPublishes().size() == 0
          outProcessingPublishes().size() == 0
        }
    cleanup:
        sessionService.close()
  }

  def "should discard old session and create a fresh"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_2"
        def oldSession = sessionService.createClean(testClientId).block()
        if (oldSession instanceof ConfigurableNetworkMqttSession) {
          oldSession.expiryInterval(Duration.ofMinutes(5))
        }
        def topicNameMappingFromOldSession = oldSession.topicNameMapping()
        topicNameMappingFromOldSession.update(1, TopicName.valueOf("topic/1"))
        topicNameMappingFromOldSession.update(2, TopicName.valueOf("topic/2"))
        topicNameMappingFromOldSession.update(3, TopicName.valueOf("topic/3"))
        sessionService.store(testClientId, oldSession).block()
    when:
        def freshSession = sessionService.createClean(testClientId).block()
    then:
        freshSession != oldSession 
        with(freshSession) {
          clientId() == testClientId
          topicNameMapping() != topicNameMappingFromOldSession
          topicNameMapping().size() == 0
        }
        topicNameMappingFromOldSession.size() == 0
    cleanup:
        sessionService.close()
  }

  def "should restore expirable old session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_3"
        def oldSession = sessionService.createClean(testClientId).block()
        if (oldSession instanceof ConfigurableNetworkMqttSession) {
          oldSession.expiryInterval(Duration.ofMinutes(5))
        }
        def topicNameMappingFromOldSession = oldSession.topicNameMapping()
        topicNameMappingFromOldSession.update(1, TopicName.valueOf("topic/1"))
        topicNameMappingFromOldSession.update(2, TopicName.valueOf("topic/2"))
        topicNameMappingFromOldSession.update(3, TopicName.valueOf("topic/3"))
        sessionService.store(testClientId, oldSession).block()
    when:
        def restoredSession = sessionService.restore(testClientId).block()
    then:
        restoredSession == oldSession
        with(restoredSession) {
          clientId() == testClientId
          topicNameMapping() == topicNameMappingFromOldSession
          topicNameMapping().size() == 3
        }
    cleanup:
        sessionService.close()
  }
}
