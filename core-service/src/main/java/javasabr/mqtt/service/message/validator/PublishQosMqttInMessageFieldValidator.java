package javasabr.mqtt.service.message.validator;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.QoS;
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
public class PublishQosMqttInMessageFieldValidator extends
    MqttInMessageFieldValidator<NetworkMqttUser, PublishMqttInMessage> {
  
  MessageOutFactoryService messageOutFactoryService;
  
  @Override
  public boolean validate(MqttConnection connection, NetworkMqttUser user, PublishMqttInMessage message) {
    QoS requestedQos = message.qos();
    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
    if (connectionConfig.maxQos().isLowerThan(requestedQos)) {
      log.warning(user.clientId(), requestedQos, "[%s] Requested QoS:[%s] is not supported"::formatted);
      handleNotSupportedQos(user);
      return false;
    }
    return true;
  }

  private void handleNotSupportedQos(NetworkMqttUser user) {
    user.closeWithReason(messageOutFactoryService
        .resolveFactory(user)
        .newDisconnect(user, DisconnectReasonCode.QOS_NOT_SUPPORTED));
  }
}
