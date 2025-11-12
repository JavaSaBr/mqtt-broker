package javasabr.mqtt.model;

public interface MqttProtocolErrors {
  String NO_ANY_TOPIC_FILTER = "Not provided any information about TopicFilters";
  String NO_ANY_TOPIC_NANE = "Not provided any information about TopicName";
  String INVALID_TOPIC_ALIAS = "Provided invalid TopicAlias";
  String INVALID_PAYLOAD_FORMAT = "Provided invalid PayloadFormat";
  String INVALID_MESSAGE_EXPIRY_INTERVAL = "Provided invalid MessageExpiryInterval";
  String INVALID_RESPONSE_TOPIC_NAME = "Provided invalid ResponseTopicName";
  String UNSUPPORTED_QOS_OR_RETAIN_HANDLING = "Unsupported 'QoS' or 'RetainHandling'";
  String MISSED_REQUIRED_MESSAGE_ID = "'Packet Identifier' must be presented'";
  String PROTOCOL_LEVEL_UNSUPPORTED_NO_LOCAL_OPTION = "'NoLocal' option is not available on this protocol level";
  String PROTOCOL_LEVEL_UNSUPPORTED_RETAIN_AS_PUBLISH_OPTION = "'RetainAsPublished' option is not available on this protocol level";
  String PROTOCOL_LEVEL_UNSUPPORTED_RETAIN_HANDLING_OPTION = "'RetainHandling' option is not available on this protocol level";
}
