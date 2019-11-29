package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.packet.in.PublishReceivedInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishReceiveInPacketHandler extends PendingOutResponseInPacketHandler<PublishReceivedInPacket> {}
