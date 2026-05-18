package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishPayloadMqttInMessageFieldValidator extends
    MqttInMessageFieldValidator<NetworkMqttUser, PublishMqttInMessage> {
 
  public static final int ORDER = 10;

  MessageOutFactoryService messageOutFactoryService;
  
  @Override
  public boolean isNotValid(MqttConnection connection, NetworkMqttUser user, PublishMqttInMessage message) {
    byte[] payload = message.payload();
    if (payload == null) {
      log.warn(user.clientId(), "[%s] Missed payload"::formatted);
      return true;
    }
    PayloadFormat payloadFormat = message.payloadFormat();
    if (payloadFormat == PayloadFormat.INVALID) {
      log.warn(user.clientId(), "[%s] Provided invalid PayloadFormat"::formatted);
      handleInvalidPayloadFormat(user);
      return true;
    }
    return false;
  }

  @Override
  public int order() {
    return ORDER;
  }
  
  private void handleInvalidPayloadFormat(NetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(
            user,
            DisconnectReasonCode.PROTOCOL_ERROR,
            MqttProtocolErrors.PROVIDED_INVALID_PAYLOAD_FORMAT));
  }
}
