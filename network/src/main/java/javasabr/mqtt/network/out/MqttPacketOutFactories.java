package javasabr.mqtt.network.out;

import javasabr.mqtt.model.MqttVersion;
import javasabr.rlib.common.util.ArrayUtils;

public class MqttPacketOutFactories {

  private static final MqttPacketOutFactory[] FACTORIES = ArrayUtils
      .array(
          new Mqtt311PacketOutFactory(),
          new Mqtt311PacketOutFactory(),
          new Mqtt5PacketOutFactory());

  public static MqttPacketOutFactory of(MqttVersion version) {
    return FACTORIES[version.ordinal()];
  }
}
