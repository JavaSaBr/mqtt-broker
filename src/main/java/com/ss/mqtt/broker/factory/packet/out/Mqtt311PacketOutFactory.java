package com.ss.mqtt.broker.factory.packet.out;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.model.data.type.StringPair;
import com.ss.mqtt.broker.model.reason.code.*;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.out.*;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

public class Mqtt311PacketOutFactory extends MqttPacketOutFactory {

    @Override
    public @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent,
        @NotNull String requestedClientId,
        long requestedSessionExpiryInterval,
        int requestedKeepAlive,
        int requestedReceiveMax,
        @NotNull String reason,
        @NotNull String serverReference,
        @NotNull String responseInformation,
        @NotNull String authenticationMethod,
        @NotNull byte[] authenticationData,
        @NotNull Array<StringPair> userProperties
    ) {
        return new ConnectAck311OutPacket(reasonCode, sessionPresent);
    }

    @Override
    public @NotNull PublishOutPacket newPublish(
        int packetId,
        @NotNull QoS qos,
        boolean retained,
        boolean duplicate,
        @NotNull String topicName,
        int topicAlias,
        @NotNull byte[] payload,
        boolean stringPayload,
        @NotNull String responseTopic,
        @NotNull byte[] correlationData,
        @NotNull Array<StringPair> userProperties
    ) {
        return new Publish311OutPacket(
            packetId,
            qos,
            retained,
            duplicate,
            topicName,
            payload
        );
    }

    @Override
    public @NotNull MqttWritablePacket newPublishAck(
        int packetId,
        @NotNull PublishAckReasonCode reasonCode,
        @NotNull String reason,
        @NotNull Array<StringPair> userProperties
    ) {
        return new PublishAck311OutPacket(packetId);
    }

    @Override
    public @NotNull MqttWritablePacket newSubscribeAck(
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes,
        @NotNull String reason,
        @NotNull Array<StringPair> userProperties
    ) {
        return new SubscribeAck311OutPacket(reasonCodes, packetId);
    }

    @Override
    public @NotNull MqttWritablePacket newUnsubscribeAck(
        int packetId,
        @NotNull Array<UnsubscribeAckReasonCode> reasonCodes,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new UnsubscribeAck311OutPacket(packetId);
    }

    @Override
    public @NotNull MqttWritablePacket newDisconnect(
        @NotNull MqttClient client,
        @NotNull DisconnectReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason,
        @NotNull String serverReference
    ) {
        return new Disconnect311OutPacket();
    }

    @Override
    public @NotNull MqttWritablePacket newAuthenticate(
        @NotNull AuthenticateReasonCode reasonCode,
        @NotNull String authenticateMethod,
        @NotNull byte[] authenticateData,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull MqttWritablePacket newPingRequest() {
        return new PingRequest311OutPacket();
    }

    @Override
    public @NotNull MqttWritablePacket newPingResponse() {
        return new PingResponse311OutPacket();
    }

    @Override
    public @NotNull MqttWritablePacket newPublishRelease(
        int packetId,
        @NotNull PublishReleaseReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishRelease311OutPacket(packetId);
    }

    @Override
    public @NotNull MqttWritablePacket newPublishReceived(
        int packetId,
        @NotNull PublishReceivedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishReceived311OutPacket(packetId);
    }

    @Override
    public @NotNull MqttWritablePacket newPublishCompleted(
        int packetId,
        @NotNull PublishCompletedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishComplete311OutPacket(packetId);
    }
}
