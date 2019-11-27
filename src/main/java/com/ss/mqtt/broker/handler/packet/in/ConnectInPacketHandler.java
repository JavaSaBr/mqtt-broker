package com.ss.mqtt.broker.handler.packet.in;

import static com.ss.mqtt.broker.model.MqttPropertyConstants.*;
import static com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD;
import static com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID;
import static com.ss.mqtt.broker.util.ReactorUtils.ifTrue;
import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.exception.MalformedPacketMqttException;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.service.AuthenticationService;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.mqtt.broker.service.MqttSessionService;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
public class ConnectInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, ConnectInPacket> {

    private final @NotNull ClientIdRegistry clientIdRegistry;
    private final @NotNull AuthenticationService authenticationService;
    private final @NotNull MqttSessionService mqttSessionService;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull ConnectInPacket packet) {

        var connection = client.getConnection();
        connection.setMqttVersion(packet.getMqttVersion());

        if (checkPacketException(client, packet)) {
            return;
        }

        authenticationService.auth(packet.getUsername(), packet.getPassword())
            .flatMap(ifTrue(client, packet, this::registerClient, BAD_USER_NAME_OR_PASSWORD, client::reject))
            .flatMap(ifTrue(client, packet, this::restoreSession, CLIENT_IDENTIFIER_NOT_VALID, client::reject))
            .subscribe();
    }

    private @NotNull Mono<Boolean> registerClient(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet
    ) {

        var requestedClientId = packet.getClientId();

        if (StringUtils.isNotEmpty(requestedClientId)) {
            return clientIdRegistry.register(requestedClientId)
                .map(ifTrue(requestedClientId, client::setClientId));
        } else {
            return clientIdRegistry.generate()
                .flatMap(newClientId -> clientIdRegistry.register(newClientId)
                    .map(ifTrue(newClientId, client::setClientId)));
        }
    }

    private @NotNull Mono<Boolean> restoreSession(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet
    ) {

        if (packet.isCleanStart()) {
            return mqttSessionService.create(client.getClientId())
                .flatMap(session -> onConnected(client, packet, session, false));
        } else {
            return mqttSessionService.restore(client.getClientId())
                .flatMap(session -> onConnected(client, packet, session, true))
                .switchIfEmpty(Mono.defer(() -> mqttSessionService.create(client.getClientId())
                    .flatMap(session -> onConnected(client, packet, session, false))));
        }
    }

    private Mono<Boolean> onConnected(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet,
        @NotNull MqttSession session,
        boolean sessionRestored
    ) {

        var connection = client.getConnection();
        var config = connection.getConfig();

        // if it was closed in parallel
        if (connection.isClosed() && config.isSessionsEnabled()) {
            // store the session again
            return mqttSessionService.store(client.getClientId(), session, config.getDefaultSessionExpiryInterval());
        }

        // select result keep alive time
        var minimalKeepAliveTime = Math.max(config.getMinKeepAliveTime(), packet.getKeepAlive());
        var keepAlive = config.isKeepAliveEnabled() ? minimalKeepAliveTime : SERVER_KEEP_ALIVE_DISABLED;

        // select result session expiry interval
        var sessionExpiryInterval = config.isSessionsEnabled() ?
            packet.getSessionExpiryInterval() : SESSION_EXPIRY_INTERVAL_DISABLED;

        if (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_UNDEFINED) {
            sessionExpiryInterval = config.getDefaultSessionExpiryInterval();
        }

        // select result receive max
        var receiveMax = packet.getReceiveMax() == RECEIVE_MAXIMUM_UNDEFINED ?
            config.getReceiveMaximum() : Math.min(packet.getReceiveMax(), config.getReceiveMaximum());

        // select result maximum packet size
        var maximumPacketSize = packet.getMaximumPacketSize() == MAXIMUM_PACKET_SIZE_UNDEFINED ?
            config.getMaximumPacketSize() : Math.min(packet.getMaximumPacketSize(), config.getMaximumPacketSize());

        // select result topic alias maximum
        var topicAliasMaximum = packet.getTopicAliasMaximum() == TOPIC_ALIAS_MAXIMUM_UNDEFINED ?
            TOPIC_ALIAS_MAXIMUM_DISABLED : Math.min(packet.getTopicAliasMaximum(), config.getTopicAliasMaximum());

        client.setSession(session);
        client.configure(
            sessionExpiryInterval,
            receiveMax,
            maximumPacketSize,
            topicAliasMaximum,
            keepAlive,
            packet.isRequestResponseInformation(),
            packet.isRequestProblemInformation()
        );

        var response = client.getPacketOutFactory().newConnectAck(
            client,
            ConnectAckReasonCode.SUCCESS,
            sessionRestored,
            packet.getClientId(),
            packet.getSessionExpiryInterval(),
            packet.getKeepAlive(),
            packet.getReceiveMax()
        );

        return Mono.fromFuture(client.sendWithFeedback(response)
            .thenApply(result -> {

                if (!result) {
                    log.warn("Was issue with sending conn ack packet to client {}", client.getClientId());
                    return false;
                }

                session.resendPendingPackets(client);
                return true;
            }));
    }

    private boolean checkPacketException(@NotNull UnsafeMqttClient client, @NotNull ConnectInPacket packet) {

        var exception = packet.getException();

        if (exception instanceof ConnectionRejectException) {
            client.reject(((ConnectionRejectException) exception).getReasonCode());
            return true;
        } else if (exception instanceof MalformedPacketMqttException) {
            client.reject(ConnectAckReasonCode.MALFORMED_PACKET);
            return true;
        }

        return false;
    }
}
