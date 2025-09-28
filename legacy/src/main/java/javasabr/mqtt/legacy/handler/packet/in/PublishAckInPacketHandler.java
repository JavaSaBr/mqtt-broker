package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.network.packet.in.PublishAckInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishAckInPacketHandler extends PendingOutResponseInPacketHandler<PublishAckInPacket> {}
