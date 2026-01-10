package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.base.util.ReactorUtils.ifTrue;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.exception.ConnectionRejectException;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.ConnectMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.ConfigurableNetworkMqttSession;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.message.validator.ClientIdMqttInMessageFieldValidator;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectInMqttInMessageHandler
    extends FieldsValidatedMqttInMessageHandler<ExternalNetworkMqttUser, ConnectMqttInMessage> {

  ClientIdRegistry clientIdRegistry;
  AuthenticationService authenticationService;
  MqttSessionService sessionService;
  SubscriptionService subscriptionService;

  public ConnectInMqttInMessageHandler(
      ClientIdRegistry clientIdRegistry,
      AuthenticationService authenticationService,
      MqttSessionService sessionService,
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(
        ExternalNetworkMqttUser.class,
        ConnectMqttInMessage.class, 
        messageOutFactoryService,
        List.of(new ClientIdMqttInMessageFieldValidator(messageOutFactoryService)));
    this.clientIdRegistry = clientIdRegistry;
    this.authenticationService = authenticationService;
    this.sessionService = sessionService;
    this.subscriptionService = subscriptionService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.CONNECT;
  }

  @Override
  protected boolean requireSession() {
    return false;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      ConnectMqttInMessage message) {
    resolveClientConnectionConfig(user, message);
    super.processValidMessage(connection, user, message);
  }

  @Override
  protected void processMessageWithValidFields(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      ConnectMqttInMessage message) {
    MqttCredentials mqttCredentials = new MqttCredentials(
        message.username(),
        message.password(),
        AuthenticationMethod.fromValue(message.authenticationMethod()),
        message.authenticationData());
    authenticationService
        .authenticate(mqttCredentials)
        .flatMap(ifTrue(
            user,
            message, this::registerClient, BAD_USER_NAME_OR_PASSWORD, connectAckReasonCode -> reject(user, connectAckReasonCode)))
        .flatMap(ifTrue(
            user,
            message, this::restoreSession, ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID, connectAckReasonCode -> reject(user, connectAckReasonCode)))
        .subscribe();
  }
  
  private void reject(ExternalNetworkMqttUser user, ConnectAckReasonCode connectAckReasonCode) {
    user.sendInBackground(messageOutFactoryService
        .resolveFactory(user)
        .newConnectAck(user, connectAckReasonCode));
  }

  private Mono<Boolean> registerClient(ExternalNetworkMqttUser user, ConnectMqttInMessage networkPacket) {

    String requestedClientId = networkPacket.clientId();
    if (StringUtils.isNotEmpty(requestedClientId)) {
      return clientIdRegistry
          .register(requestedClientId)
          .map(ifTrue(requestedClientId, user::clientId));
    }

    MqttVersion mqttVersion = user
        .connection()
        .clientConnectionConfig()
        .mqttVersion();

    // we can't assign generated client id for mqtt version less than 5
    if (mqttVersion.isLowerThan(MqttVersion.MQTT_5)) {
      return Mono.just(false);
    }

    return clientIdRegistry
        .generate()
        .flatMap(newClientId -> clientIdRegistry
            .register(newClientId)
            .map(ifTrue(newClientId, user::clientId)));
  }

  private Mono<Boolean> restoreSession(ConfigurableNetworkMqttUser user, ConnectMqttInMessage message) {
    if (message.cleanStart()) {
      return sessionService
          .createClean(user.clientId())
          .flatMap(session -> onConnected(user, session, message, false));
    } else {
      return sessionService
          .restore(user.clientId())
          .flatMap(session -> onConnected(user, session, message, true))
          .switchIfEmpty(Mono.defer(() -> sessionService
              .createClean(user.clientId())
              .flatMap(session -> onConnected(user, session, message, false))));
    }
  }

  private void resolveClientConnectionConfig(
      ConfigurableNetworkMqttUser user,
      ConnectMqttInMessage message) {

    MqttConnection connection = user.connection();
    MqttServerConnectionConfig serverConfig = connection.serverConnectionConfig();

    // select result keep alive time
    int minKeepAliveTime = Math.max(serverConfig.minKeepAliveTime(), message.keepAlive());
    int keepAlive = serverConfig.keepAliveEnabled() ? minKeepAliveTime : MqttProperties.SERVER_KEEP_ALIVE_DISABLED;

    // select result receive max
    int receiveMaxPublishes = message.receiveMaxPublishes() == MqttProperties.RECEIVE_MAX_PUBLISHES_IS_NOT_SET
                              ? serverConfig.receiveMaxPublishes()
                              : Math.min(message.receiveMaxPublishes(), serverConfig.receiveMaxPublishes());

    // select result maximum message size
    var maxMessageSize = message.maxMessageSize() == MqttProperties.MAX_MESSAGE_SIZE_IS_NOT_SET
                         ? serverConfig.maxMessageSize()
                         : Math.min(message.maxMessageSize(), serverConfig.maxMessageSize());

    // select result topic alias maximum
    var topicAliasMaxValue = message.topicAliasMaxValue() == MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET
                             ? MqttProperties.TOPIC_ALIAS_MAX_DISABLED
                             : Math.min(message.topicAliasMaxValue(), serverConfig.topicAliasMaxValue());

    connection.configure(new MqttClientConnectionConfig(
        serverConfig,
        serverConfig.maxQos(),
        message.mqttVersion(),
        resolveSessionExpiryInterval(message, serverConfig),
        receiveMaxPublishes,
        maxMessageSize,
        topicAliasMaxValue,
        keepAlive,
        message.requestResponseInformation(),
        message.requestProblemInformation()));
  }
  
  private Duration resolveSessionExpiryInterval(
      ConnectMqttInMessage message, 
      MqttServerConnectionConfig serverConfig) {
    // do not store such sessions after closing connection
    if (!serverConfig.sessionsEnabled()) {
      return MqttProperties.SESSION_EXPIRY_DURATION_DISABLED;
    }
    long expiryInterval = message.sessionExpiryInterval();
    return switch (expiryInterval) {
      case MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY -> MqttProperties.SESSION_EXPIRY_DURATION_INFINITY;
      case MqttProperties.SESSION_EXPIRY_INTERVAL_DISABLED -> MqttProperties.SESSION_EXPIRY_DURATION_DISABLED;
      default -> Duration.ofSeconds(expiryInterval);
    };
  }

  private Mono<Boolean> onConnected(
      ConfigurableNetworkMqttUser user,
      NetworkMqttSession session,
      ConnectMqttInMessage message,
      boolean sessionRestored) {

    MqttConnection connection = user.connection();
    MqttServerConnectionConfig serverConfig = connection.serverConnectionConfig();
    MqttClientConnectionConfig clientConfig = connection.clientConnectionConfig();

    if (session instanceof ConfigurableNetworkMqttSession configurableSession) {
      configurableSession.expiryInterval(clientConfig.sessionExpiryInterval());
    }

    // if it was closed in parallel
    if (connection.closed() && serverConfig.sessionsEnabled()) {
      // store the session again
      return sessionService.store(user.clientId(), session);
    }

    user.session(session);

    // already validated
    String requestedClientId = Objects.requireNonNull(message.clientId());
    long requestedSessionExpiryInterval = message.sessionExpiryInterval();
    int requestedKeepAlive = message.keepAlive();
    int requestedReceiveMaxPublishes = message.receiveMaxPublishes();

    var connectAck = messageOutFactoryService
        .resolveFactory(user)
        .newConnectAck(
            user,
            ConnectAckReasonCode.SUCCESS,
            sessionRestored,
            requestedClientId,
            requestedSessionExpiryInterval,
            requestedKeepAlive,
            requestedReceiveMaxPublishes);

    subscriptionService.restoreSubscriptions(user, session);

    return Mono.fromFuture(user
        .sendAsync(connectAck)
        .thenApply(result -> onSentConnAck(user, session, result)));
  }

  private boolean onSentConnAck(ConfigurableNetworkMqttUser user, NetworkMqttSession session, boolean result) {
    if (!result) {
      log.warning(user.clientId(), "Was issue with sending conn ack packet to client:[%s]"::formatted);
      return false;
    }
    session.resendNotConfirmedPublishesTo(user);
    return true;
  }

  @Override
  protected boolean processInvalidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      ConnectMqttInMessage message) {
    Exception exception = message.exception();
    if (exception instanceof ConnectionRejectException cre) {
      MqttOutMessage feedback = messageOutFactoryService
          .resolveFactory(user)
          .newConnectAck(user, cre.reasonCode());
      user.closeWithReason(feedback);
      return true;
    }
    return super.processInvalidMessage(connection, user, session, message);
  }
}
