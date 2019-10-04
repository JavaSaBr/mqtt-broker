package com.ss.mqtt.broker.network.packet.in;

import com.ss.rlib.network.annotation.PacketDescription;

@PacketDescription(id = 5)
public class PublishReceivedInPacket extends MqttReadablePacket {}
