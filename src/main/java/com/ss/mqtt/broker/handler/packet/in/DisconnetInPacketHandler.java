package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.model.reason.code.DisconnectReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.DisconnectInPacket;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
public class DisconnetInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, DisconnectInPacket> {

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull DisconnectInPacket packet) {

        var reasonCode = packet.getReasonCode();

        if (reasonCode == DisconnectReasonCode.NORMAL_DISCONNECTION) {
            log.info("Disconnect client {}", client);
        } else {
            log.error("Disconnect client {} by error reason {}", client, reasonCode);
        }

        client.getConnection().close();
    }
}
