package com.ss.mqtt.broker.test.integration.service

import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode
import com.ss.mqtt.broker.test.integration.IntegrationSpecification
import org.springframework.test.context.TestPropertySource
import spock.lang.Unroll

import java.util.concurrent.CompletionException

import static com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED
import static com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED

@TestPropertySource(locations = "classpath:disabled-features.properties")
class DisabledFeaturesSubscribtionServiceTest extends IntegrationSpecification {
    
    @Unroll
    def "should reject subscribe with wrong topic filter"(
        String wrongTopicFilter,
        Mqtt5SubAckReasonCode reasonCode
    ) {
        given:
            def subscriber = buildExternalMqtt5Client()
        when:
            subscriber.connectWith()
                .send()
                .join()
            subscriber.subscribeWith()
                .topicFilter(wrongTopicFilter)
                .send()
                .join()
        then:
            Thread.sleep(10)
            subscriber.state == MqttClientState.DISCONNECTED
            def ex = thrown CompletionException
            if (ex.cause != null) {
                ex.cause.class == Mqtt5SubAckException
                ex.cause.message == "SUBACK contains only Error Codes"
                ((Mqtt5SubAckException) ex.cause).mqttMessage.reasonCodes.contains(reasonCode)
            }
        where:
            wrongTopicFilter             | reasonCode
            "topic/+"                    | WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED
            "topic/#"                    | WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED
            "topic/+/Filter"             | WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED
            "\$share/group/topic/Filter" | SHARED_SUBSCRIPTIONS_NOT_SUPPORTED
    }
}
