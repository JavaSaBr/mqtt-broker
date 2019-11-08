package com.ss.mqtt.broker.network.packet.in.handler;

import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.DisconnectInPacket;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
public class DisconnetInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, DisconnectInPacket> {

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull DisconnectInPacket packet) {
        log.info("Disconnect client {} with packet {}", client, packet);
        client.release().subscribe();
    }
}
