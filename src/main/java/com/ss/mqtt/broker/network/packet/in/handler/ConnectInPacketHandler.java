package com.ss.mqtt.broker.network.packet.in.handler;

import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ConnectInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, ConnectInPacket> {

    private final ClientIdRegistry clientIdRegistry;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull ConnectInPacket packet) {

        var connection = client.getConnection();
        connection.setMqttVersion(packet.getMqttVersion());

        if (checkPacketException(client, packet)) {
            return;
        }

        var requestedClientId = packet.getClientId();

        // if we should assign our client id
        if (StringUtils.isEmpty(requestedClientId)) {
            clientIdRegistry.generate()
                .thenCompose(newClientId -> {
                    client.setClientId(newClientId);
                    return clientIdRegistry.register(newClientId);
                })
                .thenAccept(result -> {
                    if(!result) {
                        client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID);
                    } else {
                        onConnected(client, packet, requestedClientId);
                    }
                });
        } else {
            clientIdRegistry.register(requestedClientId)
                .thenAccept(result -> {
                    if(!result) {
                        client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID);
                    } else {
                        client.setClientId(requestedClientId);
                        onConnected(client, packet, requestedClientId);
                    }
                });
        }
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
        }

        return false;
    }
}
