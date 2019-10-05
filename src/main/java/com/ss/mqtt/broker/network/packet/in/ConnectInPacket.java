package com.ss.mqtt.broker.network.packet.in;

import com.ss.rlib.network.annotation.PacketDescription;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@PacketDescription(id = 1)
public class ConnectInPacket extends MqttReadablePacket {}
