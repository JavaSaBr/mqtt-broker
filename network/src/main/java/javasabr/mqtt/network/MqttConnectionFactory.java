package javasabr.mqtt.network;

import java.nio.channels.AsynchronousSocketChannel;
import javasabr.rlib.network.Connection;
import javasabr.rlib.network.Network;

public interface MqttConnectionFactory<C extends Connection<C>> {

  C newConnection(Network<C> network, AsynchronousSocketChannel channel);
}
