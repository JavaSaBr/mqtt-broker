package com.ss.mqtt.broker.network.packet.in;

import com.ss.rlib.network.annotation.PacketDescription;

@PacketDescription(id = 12)
public class PingRequestInPacket extends MqttReadablePacket {}
