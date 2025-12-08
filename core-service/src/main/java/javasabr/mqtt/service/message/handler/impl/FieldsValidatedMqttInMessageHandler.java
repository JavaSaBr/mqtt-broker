package javasabr.mqtt.service.message.handler.impl;

import java.util.List;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.message.validator.MqttInMessageFieldValidator;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class FieldsValidatedMqttInMessageHandler<U extends NetworkMqttUser, M extends MqttInMessage>
    extends AbstractMqttInMessageHandler<U, M> {

  MqttInMessageFieldValidator<? super U, M>[] fieldValidators;

  protected FieldsValidatedMqttInMessageHandler(
      Class<U> expectedUser,
      Class<M> expectedMessage,
      MessageOutFactoryService messageOutFactoryService,
      List<? extends MqttInMessageFieldValidator<? super U, M>> fieldValidators) {
    super(expectedUser, expectedMessage, messageOutFactoryService);
    //noinspection unchecked
    this.fieldValidators = fieldValidators.toArray(MqttInMessageFieldValidator[]::new);
  }

  @Override
  protected void processValidMessage(MqttConnection connection, U user, M message) {
    for (MqttInMessageFieldValidator<? super U, M> fieldValidator : fieldValidators) {
      if (!fieldValidator.validate(connection, user, message)) {
        return;
      }
    }
    super.processValidMessage(connection, user, message);
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection, 
      U user, 
      NetworkMqttSession session,
      M message) {
    for (MqttInMessageFieldValidator<? super U, M> fieldValidator : fieldValidators) {
      if (!fieldValidator.validate(connection, user, message)) {
        return;
      }
    }
    super.processValidMessage(connection, user, session, message);
  }
}
