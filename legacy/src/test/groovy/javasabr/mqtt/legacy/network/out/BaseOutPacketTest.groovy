package javasabr.mqtt.legacy.network.out

import javasabr.mqtt.legacy.network.MqttClient
import javasabr.mqtt.legacy.network.NetworkUnitSpecification
import spock.lang.Shared

class BaseOutPacketTest extends NetworkUnitSpecification {

  @Shared
  MqttClient mqtt5Client = Stub(MqttClient.UnsafeMqttClient) {
    getConnectionConfig() >> mqttConnectionConfig
    getSessionExpiryInterval() >> NetworkUnitSpecification.sessionExpiryInterval
    getReceiveMax() >> NetworkUnitSpecification.receiveMaximum
    getMaximumPacketSize() >> NetworkUnitSpecification.maximumPacketSize
    getClientId() >> clientId
    getKeepAlive() >> serverKeepAlive
    getTopicAliasMaximum() >> NetworkUnitSpecification.topicAliasMaximum
  }

  @Shared
  MqttClient mqtt311Client = Stub(MqttClient.UnsafeMqttClient) {
    getConnectionConfig() >> mqttConnectionConfig
    getSessionExpiryInterval() >> NetworkUnitSpecification.sessionExpiryInterval
    getReceiveMax() >> NetworkUnitSpecification.receiveMaximum
    getMaximumPacketSize() >> NetworkUnitSpecification.maximumPacketSize
    getClientId() >> clientId
    getKeepAlive() >> serverKeepAlive
    getTopicAliasMaximum() >> NetworkUnitSpecification.topicAliasMaximum
  }
}
