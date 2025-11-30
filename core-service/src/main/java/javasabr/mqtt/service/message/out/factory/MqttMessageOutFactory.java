package javasabr.mqtt.service.message.out.factory;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.message.MqttMessage;
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
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import org.jspecify.annotations.Nullable;

public abstract class MqttMessageOutFactory {
  
  public abstract MqttVersion mqttVersion();

  public abstract MqttOutMessage newConnectAck(
      NetworkMqttUser user,
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
      NetworkMqttUser user,
      ConnectAckReasonCode reasonCode,
      boolean sessionPresent,
      String requestedClientId,
      long requestedSessionExpiryInterval,
      int requestedKeepAlive,
      int requestedReceiveMax) {
    return newConnectAck(
        user,
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

  public MqttOutMessage newConnectAck(NetworkMqttUser user, ConnectAckReasonCode reasonCode) {
    MqttClientConnectionConfig connectionConfig = user.connectionConfig();
    return newConnectAck(
        user,
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
    return newPublishAck(messageId, reasonCode, null, MqttMessage.EMPTY_USER_PROPERTIES);
  }

  public MqttOutMessage newPublishAck(int messageId, PublishAckReasonCode reasonCode, String reason) {
    return newPublishAck(messageId, reasonCode, reason, MqttMessage.EMPTY_USER_PROPERTIES);
  }

  public abstract MqttOutMessage newSubscribeAck(
      int messageId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      Array<StringPair> userProperties);

  public MqttOutMessage newSubscribeAck(int messageId, Array<SubscribeAckReasonCode> reasonCodes) {
    return newSubscribeAck(messageId, reasonCodes, StringUtils.EMPTY, MqttMessage.EMPTY_USER_PROPERTIES);
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
    return newUnsubscribeAck(messageId, reasonCodes, MqttMessage.EMPTY_USER_PROPERTIES, StringUtils.EMPTY);
  }

  public MqttOutMessage newUnsubscribeAck(
      int messageId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties) {
    return newUnsubscribeAck(messageId, reasonCodes, userProperties, StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newDisconnect(
      NetworkMqttUser user,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason,
      @Nullable String serverReference);

  public MqttOutMessage newDisconnect(NetworkMqttUser user, DisconnectReasonCode reasonCode) {
    return newDisconnect(
        user,
        reasonCode,
        MqttMessage.EMPTY_USER_PROPERTIES,
        null,
        null);
  }

  public MqttOutMessage newDisconnect(
      NetworkMqttUser user,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties) {
    return newDisconnect(user, reasonCode, userProperties, null, null);
  }

  public MqttOutMessage newDisconnect(
      NetworkMqttUser user,
      DisconnectReasonCode reasonCode,
      @Nullable String reason) {
    return newDisconnect(user, reasonCode, MqttMessage.EMPTY_USER_PROPERTIES, reason, null);
  }

  public abstract MqttOutMessage newAuthenticate(
      AuthenticateReasonCode reasonCode,
      @Nullable String reason,
      @Nullable String authenticateMethod,
      byte @Nullable [] authenticateData,
      Array<StringPair> userProperties);

  public MqttOutMessage newAuthenticate(
      AuthenticateReasonCode reasonCode,
      @Nullable String authenticateMethod,
      byte @Nullable [] authenticateData) {
    return newAuthenticate(
        reasonCode,
        null,
        authenticateMethod,
        authenticateData,
        MqttMessage.EMPTY_USER_PROPERTIES);
  }

  public abstract MqttOutMessage newPingRequest();

  public abstract MqttOutMessage newPingResponse();

  public abstract MqttOutMessage newPublishRelease(
      int messageId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason);

  public MqttOutMessage newPublishRelease(int messageId, PublishReleaseReasonCode reasonCode) {
    return newPublishRelease(messageId, reasonCode, MqttMessage.EMPTY_USER_PROPERTIES, StringUtils.EMPTY);
  }

  public abstract MqttOutMessage newPublishReceived(
      int messageId,
      PublishReceivedReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason);

  public MqttOutMessage newPublishReceived(int messageId, PublishReceivedReasonCode reasonCode) {
    return newPublishReceived(messageId, reasonCode, MqttMessage.EMPTY_USER_PROPERTIES, null);
  }

  public abstract MqttOutMessage newPublishCompleted(
      int messageId,
      PublishCompletedReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason);

  public MqttOutMessage newPublishCompleted(int messageId, PublishCompletedReasonCode reasonCode) {
    return newPublishCompleted(messageId, reasonCode, MqttMessage.EMPTY_USER_PROPERTIES, null);
  }
}
