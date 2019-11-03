package com.ss.mqtt.broker.network.packet.in.handler;

import static reactor.core.publisher.Mono.fromRunnable;
import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.exception.MalformedPacketMqttException;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ConnectInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, ConnectInPacket> {

    private final AuthenticationService authenticationService;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull ConnectInPacket packet) {

        var connection = client.getConnection();
        connection.setMqttVersion(packet.getMqttVersion());

        if (checkPacketException(client, packet)) {
            return;
        }

        var requestedClientId = packet.getClientId();

        authenticationService.auth(packet)
            .doOnNext(client::setClientId)
            .then(fromRunnable(() -> onConnected(client, packet, requestedClientId)))
            .doOnError(ConnectionRejectException.class, error -> client.reject(error.getReasonCode()))
            .subscribe();
    }

    private void onConnected(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet,
        @NotNull String requestedClientId
    ) {

        client.configure(
            packet.getSessionExpiryInterval(),
            packet.getReceiveMax(),
            packet.getMaximumPacketSize(),
            packet.getTopicAliasMaximum(),
            packet.getKeepAlive()
        );

        client.send(client.getPacketOutFactory().newConnectAck(
            client,
            ConnectAckReasonCode.SUCCESS,
            false,
            requestedClientId,
            packet.getSessionExpiryInterval(),
            packet.getKeepAlive()
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
