package javasabr.mqtt.service.message.converter;

public interface MessageConverter<In, Out> {

  Out convert(In message);
}
