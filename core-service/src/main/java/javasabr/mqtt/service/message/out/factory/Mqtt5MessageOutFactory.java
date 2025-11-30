package javasabr.mqtt.service.message.out.factory;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.PayloadFormat;
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
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.message.out.AuthenticationMqtt5OutMessage;
import javasabr.mqtt.network.message.out.ConnectAckMqtt5OutMessage;
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.message.out.PublishAckMqtt5OutMessage;
import javasabr.mqtt.network.message.out.PublishCompleteMqtt5OutMessage;
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage;
import javasabr.mqtt.network.message.out.PublishReceivedMqtt5OutMessage;
import javasabr.mqtt.network.message.out.PublishReleaseMqtt5OutMessage;
import javasabr.mqtt.network.message.out.SubscribeAckMqtt5OutMessage;
import javasabr.mqtt.network.message.out.UnsubscribeAckMqtt5OutMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.rlib.collections.array.Array;
import org.jspecify.annotations.Nullable;

public class Mqtt5MessageOutFactory extends Mqtt311MessageOutFactory {

  @Override
  public MqttVersion mqttVersion() {
    return MqttVersion.MQTT_5;
  }

  @Override
  public MqttOutMessage newConnectAck(
      NetworkMqttUser user,
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
    return new ConnectAckMqtt5OutMessage(
        reasonCode,
        sessionPresent,
        user.clientId(),
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
  public MqttOutMessage newPublish(
      int messageId,
      QoS qos,
      boolean retain,
      boolean duplicate,
      TopicName topicName,
      int topicAlias,
      byte[] payload,
      PayloadFormat payloadFormat,
      @Nullable TopicName responseTopic,
      byte @Nullable [] correlationData,
      Array<StringPair> userProperties) {
    return new PublishMqtt5OutMessage(
        messageId,
        qos,
        retain,
        duplicate,
        topicName,
        payload,
        topicAlias,
        payloadFormat,
        responseTopic,
        correlationData,
        userProperties);
  }

  @Override
  public MqttOutMessage newPublishAck(
      int messageId,
      PublishAckReasonCode reasonCode,
      @Nullable String reason,
      Array<StringPair> userProperties) {
    return new PublishAckMqtt5OutMessage(messageId, reasonCode, reason, userProperties);
  }

  @Override
  public MqttOutMessage newSubscribeAck(
      int messageId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      Array<StringPair> userProperties) {
    return new SubscribeAckMqtt5OutMessage(messageId, reasonCodes, userProperties, reason);
  }

  @Override
  public MqttOutMessage newUnsubscribeAck(
      int messageId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason) {
    return new UnsubscribeAckMqtt5OutMessage(messageId, reasonCodes, userProperties, reason);
  }

  @Override
  public MqttOutMessage newDisconnect(
      NetworkMqttUser user,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason,
      String serverReference) {
    MqttClientConnectionConfig connectionConfig = user.connectionConfig();
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
      @Nullable String reason,
      @Nullable String authenticateMethod,
      byte @Nullable [] authenticateData,
      Array<StringPair> userProperties) {
    return new AuthenticationMqtt5OutMessage(
        reasonCode,
        reason,
        authenticateMethod,
        authenticateData,
        userProperties);
  }

  @Override
  public MqttOutMessage newPublishRelease(
      int messageId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    return new PublishReleaseMqtt5OutMessage(messageId, reasonCode, userProperties, reason);
  }

  @Override
  public MqttOutMessage newPublishReceived(
      int messageId,
      PublishReceivedReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason) {
    return new PublishReceivedMqtt5OutMessage(messageId, reasonCode, userProperties, reason);
  }

  @Override
  public MqttOutMessage newPublishCompleted(
      int messageId,
      PublishCompletedReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason) {
    return new PublishCompleteMqtt5OutMessage(messageId, reasonCode, userProperties, reason);
  }
}
