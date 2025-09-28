package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.network.packet.in.PublishCompleteInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishCompleteInPacketHandler extends PendingOutResponseInPacketHandler<PublishCompleteInPacket> {}
