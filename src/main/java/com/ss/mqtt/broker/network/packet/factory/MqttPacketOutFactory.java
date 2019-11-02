package com.ss.mqtt.broker.network.packet.factory;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.client.impl.DeviceMqttClient;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

public abstract class MqttPacketOutFactory {

    public abstract @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent,
        @NotNull String requestedClientId,
        long requestedSessionExpiryInterval,
        int requestedKeepAlive,
        @NotNull String reason,
        @NotNull String serverReference,
        @NotNull String responseInformation,
        @NotNull String authenticationMethod,
        @NotNull byte[] authenticationData,
        @NotNull Array<StringPair> userProperties
    );

    public @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent,
        @NotNull String requestedClientId,
        long requestedSessionExpiryInterval,
        int requestedKeepAlive
    ) {
        return newConnectAck(
            client,
            reasonCode,
            sessionPresent,
            requestedClientId,
            requestedSessionExpiryInterval,
            requestedKeepAlive,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            Array.empty()
        );
    }

    public @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode
    ) {
        return newConnectAck(
            client,
            reasonCode,
            false,
            StringUtils.EMPTY,
            client.getSessionExpiryInterval(),
            client.getKeepAlive(),
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            Array.empty()
        );
    }


    public abstract @NotNull MqttWritablePacket newPublishAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishAckReasonCode reasonCode,
        @NotNull String reason,
        @NotNull Array<StringPair> userProperties
    );

    public @NotNull MqttWritablePacket newPublishAck(
        @NotNull DeviceMqttClient client,
        int packetId,
        @NotNull PublishAckReasonCode reasonCode
    ) {
        return newPublishAck(client, packetId, reasonCode, StringUtils.EMPTY, Array.empty());
    }

    public abstract @NotNull MqttWritablePacket newSubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes,
        @NotNull String reason,
        @NotNull Array<StringPair> userProperties
    );

    public @NotNull MqttWritablePacket newSubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes
    ) {
        return newSubscribeAck(client, packetId, reasonCodes, StringUtils.EMPTY, Array.empty());
    }

    public abstract @NotNull MqttWritablePacket newUnsubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<UnsubscribeAckReasonCode> reasonCodes,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    );

    public @NotNull MqttWritablePacket newUnsubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<UnsubscribeAckReasonCode> reasonCodes
    ) {
        return newUnsubscribeAck(client, packetId, reasonCodes, Array.empty(), StringUtils.EMPTY);
    }

    public abstract @NotNull MqttWritablePacket newDisconnect(
        @NotNull MqttClient client,
        @NotNull DisconnectReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason,
        @NotNull String serverReference
    );

    public @NotNull MqttWritablePacket newDisconnect(
        @NotNull DeviceMqttClient client,
        @NotNull DisconnectReasonCode reasonCode
    ) {
        return newDisconnect(client, reasonCode, Array.empty(), StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public abstract @NotNull MqttWritablePacket newAuthenticate(
        @NotNull MqttClient client,
        @NotNull AuthenticateReasonCode reasonCode,
        @NotNull String authenticateMethod,
        @NotNull byte[] authenticateData,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    );

    public @NotNull MqttWritablePacket newAuthenticate(
        @NotNull MqttClient client,
        @NotNull AuthenticateReasonCode reasonCode,
        @NotNull String authenticateMethod,
        @NotNull byte[] authenticateData
    ) {
        return newAuthenticate(
            client,
            reasonCode,
            authenticateMethod,
            authenticateData,
            Array.empty(),
            StringUtils.EMPTY
        );
    }

    public abstract @NotNull MqttWritablePacket newPingRequest(@NotNull MqttClient client);

    public abstract @NotNull MqttWritablePacket newPingResponse(@NotNull MqttClient client);

    public abstract @NotNull MqttWritablePacket newPublishRelease(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishReleaseReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    );

    public @NotNull MqttWritablePacket newPublishRelease(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishReleaseReasonCode reasonCode
    ) {
        return newPublishRelease(client, packetId, reasonCode, Array.empty(), StringUtils.EMPTY);
    }

    public abstract @NotNull MqttWritablePacket newPublishReceived(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishReceivedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    );

    public @NotNull MqttWritablePacket newPublishReceived(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishReceivedReasonCode reasonCode
    ) {
        return newPublishReceived(client, packetId, reasonCode, Array.empty(), StringUtils.EMPTY);
    }

    public abstract @NotNull MqttWritablePacket newPublishCompleted(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishCompletedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    );

    public @NotNull MqttWritablePacket newPublishCompleted(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishCompletedReasonCode reasonCode
    ) {
        return newPublishCompleted(client, packetId, reasonCode, Array.empty(), StringUtils.EMPTY);
    }
}
