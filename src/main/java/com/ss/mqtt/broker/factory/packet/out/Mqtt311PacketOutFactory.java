package com.ss.mqtt.broker.factory.packet.out;

import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.data.type.StringPair;
import com.ss.mqtt.broker.model.reason.code.AuthenticateReasonCode;
import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.reason.code.DisconnectReasonCode;
import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode;
import com.ss.mqtt.broker.model.reason.code.PublishCompletedReasonCode;
import com.ss.mqtt.broker.model.reason.code.PublishReceivedReasonCode;
import com.ss.mqtt.broker.model.reason.code.PublishReleaseReasonCode;
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.out.ConnectAck311OutPacket;
import com.ss.mqtt.broker.network.packet.out.Disconnect311OutPacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.network.packet.out.PingRequest311OutPacket;
import com.ss.mqtt.broker.network.packet.out.PingResponse311OutPacket;
import com.ss.mqtt.broker.network.packet.out.Publish311OutPacket;
import com.ss.mqtt.broker.network.packet.out.PublishAck311OutPacket;
import com.ss.mqtt.broker.network.packet.out.PublishComplete311OutPacket;
import com.ss.mqtt.broker.network.packet.out.PublishOutPacket;
import com.ss.mqtt.broker.network.packet.out.PublishReceived311OutPacket;
import com.ss.mqtt.broker.network.packet.out.PublishRelease311OutPacket;
import com.ss.mqtt.broker.network.packet.out.SubscribeAck311OutPacket;
import com.ss.mqtt.broker.network.packet.out.UnsubscribeAck311OutPacket;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;

public class Mqtt311PacketOutFactory extends MqttPacketOutFactory {

    @Override
    public  MqttWritablePacket newConnectAck(
        MqttClient client,
        ConnectAckReasonCode reasonCode,
        boolean sessionPresent,
        String requestedClientId,
        long requestedSessionExpiryInterval,
        int requestedKeepAlive,
        int requestedReceiveMax,
        String reason,
        String serverReference,
        String responseInformation,
        String authenticationMethod,
        byte[] authenticationData,
        MutableArray<StringPair> userProperties
    ) {
        return new ConnectAck311OutPacket(reasonCode, sessionPresent);
    }

    @Override
    public PublishOutPacket newPublish(
        int packetId,
        QoS qos,
        boolean retained,
        boolean duplicate,
        String topicName,
        int topicAlias,
        byte[] payload,
        boolean stringPayload,
        String responseTopic,
        byte[] correlationData,
        MutableArray<StringPair> userProperties
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
    public MqttWritablePacket newPublishAck(
        int packetId,
        PublishAckReasonCode reasonCode,
        String reason,
        MutableArray<StringPair> userProperties
    ) {
        return new PublishAck311OutPacket(packetId);
    }

    @Override
    public MqttWritablePacket newSubscribeAck(
        int packetId,
        Array<SubscribeAckReasonCode> reasonCodes,
        String reason,
        MutableArray<StringPair> userProperties
    ) {
        return new SubscribeAck311OutPacket(reasonCodes, packetId);
    }

    @Override
    public MqttWritablePacket newUnsubscribeAck(
        int packetId,
        Array<UnsubscribeAckReasonCode> reasonCodes,
        MutableArray<StringPair> userProperties,
        String reason
    ) {
        return new UnsubscribeAck311OutPacket(packetId);
    }

    @Override
    public MqttWritablePacket newDisconnect(
        MqttClient client,
        DisconnectReasonCode reasonCode,
        MutableArray<StringPair> userProperties,
        String reason,
        String serverReference
    ) {
        return new Disconnect311OutPacket();
    }

    @Override
    public MqttWritablePacket newAuthenticate(
        AuthenticateReasonCode reasonCode,
        String authenticateMethod,
        byte[] authenticateData,
        MutableArray<StringPair> userProperties,
        String reason
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public  MqttWritablePacket newPingRequest() {
        return new PingRequest311OutPacket();
    }

    @Override
    public  MqttWritablePacket newPingResponse() {
        return new PingResponse311OutPacket();
    }

    @Override
    public  MqttWritablePacket newPublishRelease(
        int packetId,
        PublishReleaseReasonCode reasonCode,
        MutableArray<StringPair> userProperties,
        String reason
    ) {
        return new PublishRelease311OutPacket(packetId);
    }

    @Override
    public MqttWritablePacket newPublishReceived(
        int packetId,
        PublishReceivedReasonCode reasonCode,
        MutableArray<StringPair> userProperties,
        String reason
    ) {
        return new PublishReceived311OutPacket(packetId);
    }

    @Override
    public  MqttWritablePacket newPublishCompleted(
        int packetId,
        PublishCompletedReasonCode reasonCode,
        MutableArray<StringPair> userProperties,
        String reason
    ) {
        return new PublishComplete311OutPacket(packetId);
    }
}
