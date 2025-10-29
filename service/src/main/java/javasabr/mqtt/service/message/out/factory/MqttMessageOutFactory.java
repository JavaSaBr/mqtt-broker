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
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.message.out.PublishMqttOutMessage;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import org.jspecify.annotations.Nullable;

public abstract class MqttMessageOutFactory {

  public abstract MqttVersion mqttVersion();

  public abstract MqttOutMessage newConnectAck(
      MqttClient client,
      ConnectAckReasonCode reasonCode,
      boolean sessionPresent,
      String requestedClientId,
      long requestedSessionExpiryInterval,
      int requestedKeepAlive,
      int requestedReceiveMax,
      String reason,
      String serverReference,
      String responseInformation,
      String authenticationMethod,
      byte[] authenticationData,
      Array<StringPair> userProperties);

  public MqttOutMessage newConnectAck(
      MqttClient client,
      ConnectAckReasonCode reasonCode,
      boolean sessionPresent,
      String requestedClientId,
      long requestedSessionExpiryInterval,
      int requestedKeepAlive,
      int requestedReceiveMax) {
    return newConnectAck(
        client,
        reasonCode,
        sessionPresent,
        requestedClientId,
        requestedSessionExpiryInterval,
        requestedKeepAlive,
        requestedReceiveMax,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        ArrayUtils.EMPTY_BYTE_ARRAY,
        MutableArray.ofType(StringPair.class));
  }

  public MqttOutMessage newConnectAck(MqttClient client, ConnectAckReasonCode reasonCode) {
    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    return newConnectAck(
        client,
        reasonCode,
        false,
        StringUtils.EMPTY,
        connectionConfig.sessionExpiryInterval(),
        connectionConfig.keepAlive(),
        connectionConfig.receiveMaxPublishes(),
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        ArrayUtils.EMPTY_BYTE_ARRAY,
        MutableArray.ofType(StringPair.class));
  }

  public PublishMqttOutMessage newPublish(
      int messageId,
      QoS qos,
      boolean retained,
      boolean duplicate,
      String topicName,
      byte[] payload) {
    return newPublish(
        messageId,
        qos,
        retained,
        duplicate,
        topicName,
        0,
        payload,
        false,
        StringUtils.EMPTY,
        ArrayUtils.EMPTY_BYTE_ARRAY,
        MutableArray.ofType(StringPair.class));
  }

  public abstract PublishMqttOutMessage newPublish(
      int messageId,
      QoS qos,
      boolean retained,
      boolean duplicate,
      String topicName,
      int topicAlias,
      byte[] payload,
      boolean stringPayload,
      @Nullable String responseTopic,
      byte @Nullable [] correlationData,
      Array<StringPair> userProperties);

  public abstract MqttOutMessage newPublishAck(
      int messageId,
      PublishAckReasonCode reasonCode,
      String reason,
      Array<StringPair> userProperties);

  public MqttOutMessage newPublishAck(int packetId, PublishAckReasonCode reasonCode) {
    return newPublishAck(packetId, reasonCode, StringUtils.EMPTY, Array.empty(StringPair.class));
  }

  public abstract MqttOutMessage newSubscribeAck(
      int packetId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      Array<StringPair> userProperties);

  public MqttOutMessage newSubscribeAck(int packetId, Array<SubscribeAckReasonCode> reasonCodes) {
    return newSubscribeAck(packetId, reasonCodes, StringUtils.EMPTY, Array.empty(StringPair.class));
  }

  public abstract MqttOutMessage newUnsubscribeAck(
      int packetId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason);

  public MqttOutMessage newUnsubscribeAck(int packetId, Array<UnsubscribeAckReasonCode> reasonCodes) {
    return newUnsubscribeAck(packetId, reasonCodes, Array.empty(StringPair.class), StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason,
      String serverReference);

  public MqttOutMessage newDisconnect(MqttClient client, DisconnectReasonCode reasonCode) {
    return newDisconnect(
        client,
        reasonCode,
        Array.empty(StringPair.class),
        StringUtils.EMPTY,
        StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newAuthenticate(
      AuthenticateReasonCode reasonCode,
      String authenticateMethod,
      byte[] authenticateData,
      Array<StringPair> userProperties,
      String reason);

  public MqttOutMessage newAuthenticate(
      AuthenticateReasonCode reasonCode,
      String authenticateMethod,
      byte[] authenticateData) {
    return newAuthenticate(
        reasonCode,
        authenticateMethod,
        authenticateData,
        Array.empty(StringPair.class),
        StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newPingRequest();

  public abstract MqttOutMessage newPingResponse();

  public abstract MqttOutMessage newPublishRelease(
      int packetId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason);

  public MqttOutMessage newPublishRelease(int packetId, PublishReleaseReasonCode reasonCode) {
    return newPublishRelease(packetId, reasonCode, Array.empty(StringPair.class), StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newPublishReceived(
      int packetId,
      PublishReceivedReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason);

  public MqttOutMessage newPublishReceived(int packetId, PublishReceivedReasonCode reasonCode) {
    return newPublishReceived(packetId, reasonCode, Array.empty(StringPair.class), StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newPublishCompleted(
      int packetId,
      PublishCompletedReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason);

  public MqttOutMessage newPublishCompleted(int packetId, PublishCompletedReasonCode reasonCode) {
    return newPublishCompleted(packetId, reasonCode, Array.empty(StringPair.class), StringUtils.EMPTY);
  }
}
