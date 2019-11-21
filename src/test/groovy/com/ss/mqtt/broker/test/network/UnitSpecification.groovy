package com.ss.mqtt.broker.test.network

import com.ss.mqtt.broker.config.MqttConnectionConfig
import com.ss.mqtt.broker.model.MqttVersion
import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.network.MqttConnection
import com.ss.mqtt.broker.network.client.MqttClient
import spock.lang.Specification

class UnitSpecification extends Specification {
    
    MqttConnectionConfig mqttConnectionConfig(
        QoS maxQos,
        int maximumPacketSize,
        int serverKeepAlive,
        int receiveMaximum,
        int topicAliasMaximum,
        long sessionExpiryInterval,
        boolean keepAliveEnabled,
        boolean sessionsEnabled,
        boolean retainAvailable,
        boolean wildcardSubscriptionAvailable,
        boolean subscriptionIdAvailable,
        boolean sharedSubscriptionAvailable
    
    ) {
        return new MqttConnectionConfig(
            maxQos,
            maximumPacketSize,
            serverKeepAlive,
            receiveMaximum,
            topicAliasMaximum,
            sessionExpiryInterval,
            keepAliveEnabled,
            sessionsEnabled,
            retainAvailable,
            wildcardSubscriptionAvailable,
            subscriptionIdAvailable,
            sharedSubscriptionAvailable
        )
    }
    
    MqttConnection mqttConnection(
        MqttVersion mqttVersion,
        MqttConnectionConfig mqttConnectionConfig,
        long sessionExpiryInterval,
        int receiveMaximum,
        int maximumPacketSize,
        String clientId,
        int serverKeepAlive,
        int topicAliasMaximum
    ) {
        return Stub(MqttConnection) {
            isSupported(_ as MqttVersion) >> { MqttVersion version -> version == mqttVersion }
            getConfig() >> mqttConnectionConfig
            getClient() >> mqttClient(
                mqttConnectionConfig,
                sessionExpiryInterval,
                receiveMaximum,
                maximumPacketSize,
                clientId,
                serverKeepAlive,
                topicAliasMaximum
            )
        }
    }
    
    MqttClient mqttClient(
        MqttConnectionConfig mqttConnectionConfig,
        long sessionExpiryInterval,
        int receiveMaximum,
        int maximumPacketSize,
        String clientId,
        int serverKeepAlive,
        int topicAliasMaximum
    ) {
        return Stub(MqttClient.UnsafeMqttClient) {
            getConnectionConfig() >> mqttConnectionConfig
            getSessionExpiryInterval() >> sessionExpiryInterval
            getReceiveMax() >> receiveMaximum
            getMaximumPacketSize() >> maximumPacketSize
            getClientId() >> clientId
            getKeepAlive() >> serverKeepAlive
            getTopicAliasMaximum() >> topicAliasMaximum
        }
    }
}
