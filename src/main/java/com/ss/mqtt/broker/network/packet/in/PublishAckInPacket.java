package com.ss.mqtt.broker.network.packet.in;

import com.ss.rlib.network.annotation.PacketDescription;

@PacketDescription(id = 4)
public class PublishAckInPacket extends MqttReadablePacket {}
