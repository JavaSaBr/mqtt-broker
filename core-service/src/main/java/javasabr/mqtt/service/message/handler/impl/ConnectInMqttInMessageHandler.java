package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.base.util.ReactorUtils.ifTrue;
import static javasabr.mqtt.model.MqttProperties.MAXIMUM_MESSAGE_SIZE_IS_NOT_SET;
import static javasabr.mqtt.model.MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET;
import static javasabr.mqtt.model.MqttProperties.SERVER_KEEP_ALIVE_DISABLED;
import static javasabr.mqtt.model.MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY;
import static javasabr.mqtt.model.MqttProperties.SESSION_EXPIRY_INTERVAL_IS_NOT_SET;
import static javasabr.mqtt.model.MqttProperties.TOPIC_ALIAS_MAXIMUM_DISABLED;
import static javasabr.mqtt.model.MqttProperties.TOPIC_ALIAS_MAXIMUM_IS_NOT_SET;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID;

import java.time.Duration;
import javasabr.mqtt.model.MqttClientConnectionConfig;
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
import javasabr.mqtt.service.AuthenticationService;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectInMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalNetworkMqttUser, ConnectMqttInMessage> {

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
    super(ExternalNetworkMqttUser.class, ConnectMqttInMessage.class, messageOutFactoryService);
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
    authenticationService
        .auth(message.username(), message.password())
        .flatMap(ifTrue(
            user,
            message, this::registerClient, BAD_USER_NAME_OR_PASSWORD, connectAckReasonCode -> reject(user, connectAckReasonCode)))
        .flatMap(ifTrue(
            user,
            message, this::restoreSession, CLIENT_IDENTIFIER_NOT_VALID, connectAckReasonCode -> reject(user, connectAckReasonCode)))
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

  private Mono<Boolean> restoreSession(ConfigurableNetworkMqttUser user, ConnectMqttInMessage packet) {
    if (packet.cleanStart()) {
      return sessionService
          .createClean(user.clientId())
          .flatMap(session -> onConnected(user, packet, session, false));
    } else {
      return sessionService
          .restore(user.clientId())
          .flatMap(session -> onConnected(user, packet, session, true))
          .switchIfEmpty(Mono.defer(() -> sessionService
              .createClean(user.clientId())
              .flatMap(session -> onConnected(user, packet, session, false))));
    }
  }

  private void resolveClientConnectionConfig(
      ConfigurableNetworkMqttUser user,
      ConnectMqttInMessage message) {

    MqttConnection connection = user.connection();
    MqttServerConnectionConfig serverConfig = connection.serverConnectionConfig();

    // select result keep alive time
    int minimalKeepAliveTime = Math.max(serverConfig.minKeepAliveTime(), message.keepAlive());
    int keepAlive = serverConfig.keepAliveEnabled() ? minimalKeepAliveTime : SERVER_KEEP_ALIVE_DISABLED;
    
    // select result receive max
    int receiveMaxPublishes = message.receiveMaxPublishes() == RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET
                              ? serverConfig.receiveMaxPublishes()
                              : Math.min(message.receiveMaxPublishes(), serverConfig.receiveMaxPublishes());

    // select result maximum packet size
    var maximumPacketSize = message.maxPacketSize() == MAXIMUM_MESSAGE_SIZE_IS_NOT_SET
                            ? serverConfig.maxMessageSize()
                            : Math.min(message.maxPacketSize(), serverConfig.maxMessageSize());

    // select result topic alias maximum
    var topicAliasMaxValue = message.topicAliasMaxValue() == TOPIC_ALIAS_MAXIMUM_IS_NOT_SET
                             ? TOPIC_ALIAS_MAXIMUM_DISABLED
                             : Math.min(message.topicAliasMaxValue(), serverConfig.topicAliasMaxValue());

    connection.configure(new MqttClientConnectionConfig(
        serverConfig,
        serverConfig.maxQos(),
        message.mqttVersion(),
        resolveSessionExpiryInterval(message, serverConfig),
        receiveMaxPublishes,
        maximumPacketSize,
        topicAliasMaxValue,
        keepAlive,
        message.requestResponseInformation(),
        message.requestProblemInformation()));
  }
  
  @Nullable
  private Duration resolveSessionExpiryInterval(
      ConnectMqttInMessage message, 
      MqttServerConnectionConfig serverConfig) {
    // do not store such sessions after closing connection
    if (!serverConfig.sessionsEnabled()) {
      return null;
    }
    
    // select result session expiry interval
    long expiryIntervalInSecs = message.sessionExpiryInterval();
    if (expiryIntervalInSecs == SESSION_EXPIRY_INTERVAL_IS_NOT_SET) {
      expiryIntervalInSecs = serverConfig.defaultSessionExpiryInterval();
    }
    
    if (expiryIntervalInSecs == SESSION_EXPIRY_INTERVAL_INFINITY) {
      return Duration.ZERO;
    } else {
      return Duration.ofSeconds(expiryIntervalInSecs);
    }
  }

  private Mono<Boolean> onConnected(
      ConfigurableNetworkMqttUser user,
      ConnectMqttInMessage message,
      NetworkMqttSession session,
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

    var connectAck = messageOutFactoryService
        .resolveFactory(user)
        .newConnectAck(
            user,
            ConnectAckReasonCode.SUCCESS,
            sessionRestored,
            message.clientId(),
            message.sessionExpiryInterval(),
            message.keepAlive(),
            message.receiveMaxPublishes());

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
