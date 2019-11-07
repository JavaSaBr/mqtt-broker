package com.ss.mqtt.broker.network.packet.in.handler;

import static reactor.core.publisher.Mono.fromCallable;
import static com.ss.mqtt.broker.model.MqttPropertyConstants.*;
import static reactor.core.publisher.Mono.fromRunnable;
import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.exception.MalformedPacketMqttException;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.service.AuthenticationService;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ConnectInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, ConnectInPacket> {

    private final ClientIdRegistry clientIdRegistry;
    private final AuthenticationService authenticationService;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull ConnectInPacket packet) {

        var connection = client.getConnection();
        connection.setMqttVersion(packet.getMqttVersion());

        if (checkPacketException(client, packet)) {
            return;
        }

        authenticationService.auth(packet.getUsername(), packet.getPassword())
            .filter(Boolean::booleanValue)
            .flatMap(authenticated -> registerClient(client, packet))
            .switchIfEmpty(fromRunnable(() -> client.reject(ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD)))
            .subscribe();
    }

    private @NotNull Mono<Boolean> registerClient(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet
    ) {
        var requestedClientId = packet.getClientId();
        if (StringUtils.isEmpty(requestedClientId)) {
            return processWithoutClientId(client, packet, requestedClientId);
        } else {
            return processWithClientId(client, packet, requestedClientId);
        }
    }

    private @NotNull Mono<Boolean> processWithClientId(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet,
        @NotNull String requestedClientId
    ) {
        return clientIdRegistry.register(requestedClientId)
            .filter(Boolean::booleanValue)
            .flatMap(registered -> fromCallable(() -> {
                client.setClientId(requestedClientId);
                onConnected(client, packet, requestedClientId);
                return true;
            }))
            .switchIfEmpty(fromRunnable(() -> client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID)));
    }

    private @NotNull Mono<Boolean> processWithoutClientId(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet,
        @NotNull String requestedClientId
    ) {
        return clientIdRegistry.generate()
            .doOnNext(client::setClientId)
            .flatMap(clientIdRegistry::register)
            .filter(Boolean::booleanValue)
            .flatMap(registered -> fromCallable(() -> {
                onConnected(client, packet, requestedClientId);
                return true;
            }))
            .switchIfEmpty(fromRunnable(() -> client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID)));
    }

    private void onConnected(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet,
        @NotNull String requestedClientId
    ) {

        var connection = client.getConnection();
        var config = connection.getConfig();

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

        client.configure(
            sessionExpiryInterval,
            receiveMax,
            maximumPacketSize,
            topicAliasMaximum,
            keepAlive,
            packet.isRequestResponseInformation(),
            packet.isRequestProblemInformation()
        );

        client.send(client.getPacketOutFactory().newConnectAck(
            client,
            ConnectAckReasonCode.SUCCESS,
            false,
            requestedClientId,
            packet.getSessionExpiryInterval(),
            packet.getKeepAlive(),
            packet.getReceiveMax()
        ));
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
