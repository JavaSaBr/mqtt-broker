package javasabr.mqtt.service.message.out.factory;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.out.AuthenticationMqtt5OutMessage;
import javasabr.mqtt.network.message.out.ConnectAckMqtt5OutMessage;
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.message.out.PublishAckMqtt5OutMessage;
import javasabr.mqtt.network.message.out.PublishCompleteMqtt5OutMessage;
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage;
import javasabr.mqtt.network.message.out.PublishMqttOutMessage;
import javasabr.mqtt.network.message.out.PublishReceivedMqtt5OutMessage;
import javasabr.mqtt.network.message.out.PublishReleaseMqtt5OutMessage;
import javasabr.mqtt.network.message.out.SubscribeAckMqtt5OutMessage;
import javasabr.mqtt.network.message.out.UnsubscribeAckMqtt5OutMessage;
import javasabr.rlib.collections.array.Array;

public class Mqtt5MessageOutFactory extends Mqtt311MessageOutFactory {

  @Override
  public MqttVersion mqttVersion() {
    return MqttVersion.MQTT_5;
  }

  @Override
  public MqttOutMessage newConnectAck(
      MqttClient client,
      ConnectAckReasonCode reasonCode,
      boolean sessionPresent,
      String requestedClientId,
      long requestedSessionExpiryInterval,
      int requestedKeepAlive,
      int requestedReceiveMaxPublishes,
      String reason,
      String serverReference,
      String responseInformation,
      String authenticationMethod,
      byte[] authenticationData,
      Array<StringPair> userProperties) {
    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    return new ConnectAckMqtt5OutMessage(
        connectionConfig,
        reasonCode,
        sessionPresent,
        client.clientId(),
        requestedClientId,
        requestedSessionExpiryInterval,
        requestedKeepAlive,
        requestedReceiveMaxPublishes,
        reason,
        serverReference,
        responseInformation,
        authenticationMethod,
        authenticationData,
        userProperties);
  }

  @Override
  public PublishMqttOutMessage newPublish(
      int packetId,
      QoS qos,
      boolean retained,
      boolean duplicate,
      String topicName,
      int topicAlias,
      byte[] payload,
      boolean stringPayload,
      String responseTopic,
      byte[] correlationData,
      Array<StringPair> userProperties) {
    return new PublishMqtt5OutMessage(
        packetId,
        qos,
        retained,
        duplicate,
        topicName,
        payload,
        topicAlias,
        stringPayload,
        responseTopic,
        correlationData,
        userProperties);
  }

  @Override
  public MqttOutMessage newPublishAck(
      int packetId,
      PublishAckReasonCode reasonCode,
      String reason,
      Array<StringPair> userProperties) {
    return new PublishAckMqtt5OutMessage(packetId, reasonCode, userProperties, reason);
  }

  @Override
  public MqttOutMessage newSubscribeAck(
      int packetId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      Array<StringPair> userProperties) {
    return new SubscribeAckMqtt5OutMessage(packetId, reasonCodes, userProperties, reason);
  }

  @Override
  public MqttOutMessage newUnsubscribeAck(
      int packetId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason) {
    return new UnsubscribeAckMqtt5OutMessage(packetId, reasonCodes, userProperties, reason);
  }

  @Override
  public MqttOutMessage newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason,
      String serverReference) {
    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    return new DisconnectMqtt5OutMessage(
        reasonCode,
        userProperties,
        reason,
        serverReference,
        connectionConfig.sessionExpiryInterval());
  }

  @Override
  public MqttOutMessage newAuthenticate(
      AuthenticateReasonCode reasonCode,
      String authenticateMethod,
      byte[] authenticateData,
      Array<StringPair> userProperties,
      String reason) {
    return new AuthenticationMqtt5OutMessage(userProperties, reasonCode, reason, authenticateMethod, authenticateData);
  }

  @Override
  public MqttOutMessage newPublishRelease(
      int packetId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    return new PublishReleaseMqtt5OutMessage(packetId, reasonCode, userProperties, reason);
  }

  @Override
  public MqttOutMessage newPublishReceived(
      int packetId,
      PublishReceivedReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    return new PublishReceivedMqtt5OutMessage(packetId, reasonCode, userProperties, reason);
  }

  @Override
  public MqttOutMessage newPublishCompleted(
      int packetId,
      PublishCompletedReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    return new PublishCompleteMqtt5OutMessage(packetId, reasonCode, userProperties, reason);
  }
}
