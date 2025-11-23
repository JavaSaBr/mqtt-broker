package javasabr.mqtt.service.message.out.factory;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
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
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import org.jspecify.annotations.Nullable;

public abstract class MqttMessageOutFactory {

  public static final Array<StringPair> EMPTY_USER_PROPERTIES = Array.empty(StringPair.class);

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

  public MqttOutMessage newPublish(
      int messageId,
      QoS qos,
      boolean retain,
      boolean duplicate,
      TopicName topicName,
      byte[] payload) {
    return newPublish(
        messageId,
        qos,
        retain,
        duplicate,
        topicName,
        MqttProperties.TOPIC_ALIAS_NOT_SET,
        payload,
        PayloadFormat.UNDEFINED,
        null,
        null,
        MutableArray.ofType(StringPair.class));
  }

  public abstract MqttOutMessage newPublish(
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
      Array<StringPair> userProperties);

  public abstract MqttOutMessage newPublishAck(
      int messageId,
      PublishAckReasonCode reasonCode,
      @Nullable String reason,
      Array<StringPair> userProperties);

  public MqttOutMessage newPublishAck(int messageId, PublishAckReasonCode reasonCode) {
    return newPublishAck(messageId, reasonCode, null, EMPTY_USER_PROPERTIES);
  }

  public MqttOutMessage newPublishAck(int messageId, PublishAckReasonCode reasonCode, String reason) {
    return newPublishAck(messageId, reasonCode, reason, EMPTY_USER_PROPERTIES);
  }

  public abstract MqttOutMessage newSubscribeAck(
      int messageId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      Array<StringPair> userProperties);

  public MqttOutMessage newSubscribeAck(int messageId, Array<SubscribeAckReasonCode> reasonCodes) {
    return newSubscribeAck(messageId, reasonCodes, StringUtils.EMPTY, EMPTY_USER_PROPERTIES);
  }

  public MqttOutMessage newSubscribeAck(
      int messageId,
      Array<SubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties) {
    return newSubscribeAck(messageId, reasonCodes, StringUtils.EMPTY, userProperties);
  }

  public abstract MqttOutMessage newUnsubscribeAck(
      int messageId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason);

  public MqttOutMessage newUnsubscribeAck(int messageId, Array<UnsubscribeAckReasonCode> reasonCodes) {
    return newUnsubscribeAck(messageId, reasonCodes, EMPTY_USER_PROPERTIES, StringUtils.EMPTY);
  }

  public MqttOutMessage newUnsubscribeAck(
      int messageId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties) {
    return newUnsubscribeAck(messageId, reasonCodes, userProperties, StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason,
      @Nullable String serverReference);

  public MqttOutMessage newDisconnect(MqttClient client, DisconnectReasonCode reasonCode) {
    return newDisconnect(
        client,
        reasonCode,
        EMPTY_USER_PROPERTIES,
        null,
        null);
  }

  public MqttOutMessage newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties) {
    return newDisconnect(client, reasonCode, userProperties, null, null);
  }

  public MqttOutMessage newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      @Nullable String reason) {
    return newDisconnect(client, reasonCode, EMPTY_USER_PROPERTIES, reason, null);
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
        authenticateData, EMPTY_USER_PROPERTIES,
        StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newPingRequest();

  public abstract MqttOutMessage newPingResponse();

  public abstract MqttOutMessage newPublishRelease(
      int messageId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason);

  public MqttOutMessage newPublishRelease(int messageId, PublishReleaseReasonCode reasonCode) {
    return newPublishRelease(messageId, reasonCode, EMPTY_USER_PROPERTIES, StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newPublishReceived(
      int messageId,
      PublishReceivedReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason);

  public MqttOutMessage newPublishReceived(int messageId, PublishReceivedReasonCode reasonCode) {
    return newPublishReceived(messageId, reasonCode, EMPTY_USER_PROPERTIES, null);
  }

  public abstract MqttOutMessage newPublishCompleted(
      int messageId,
      PublishCompletedReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason);

  public MqttOutMessage newPublishCompleted(int messageId, PublishCompletedReasonCode reasonCode) {
    return newPublishCompleted(messageId, reasonCode, EMPTY_USER_PROPERTIES, null);
  }
}
