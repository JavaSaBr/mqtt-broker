package javasabr.mqtt.model;

public interface MqttProtocolErrors {
  String NO_ANY_TOPIC_FILTERS = "Not provided any information about 'Topic Filters'";
  String NO_ANY_TOPIC_NANE = "Not provided any information about TopicName";
  //String INVALID_TOPIC_ALIAS = "Provided invalid TopicAlias";

  String PROVIDED_INVALID_PAYLOAD_FORMAT = "Provided invalid PayloadFormat";
  String PROVIDED_INVALID_MESSAGE_EXPIRY_INTERVAL = "Provided invalid MessageExpiryInterval";
  String PROVIDED_INVALID_SESSION_EXPIRY_INTERVAL = "Provided invalid 'Session Expiry Interval'";
  String PROVIDED_INVALID_RECEIVED_MAX_PUBLISHES = "Provided invalid 'Receive Maximum'";
  String PROVIDED_INVALID_MAX_QOS = "Provided invalid 'Maximum QoS'";
  String PROVIDED_INVALID_RETAIN_AVAILABLE = "Provided invalid 'Retain Available'";
  String PROVIDED_INVALID_MAX_MESSAGE_SIZE = "Provided invalid 'Maximum Packet Size'";
  String PROVIDED_INVALID_TOPIC_ALIAS_MAX = "Provided invalid 'Topic Alias Maximum'";
  String PROVIDED_INVALID_WILDCARD_SUBSCRIPTION_AVAILABLE = "Provided invalid 'Wildcard Subscription Available'";
  String PROVIDED_INVALID_SUBSCRIPTION_IDENTIFIERS_AVAILABLE = "Provided invalid 'Subscription Identifiers Available'";
  String PROVIDED_INVALID_SHARED_SUBSCRIPTION_AVAILABLE = "Provided invalid 'Shared Subscription Available'";
  String PROVIDED_INVALID_SERVER_KEEP_ALIVE = "Provided invalid 'Server Keep Alive'";

  String INVALID_RESPONSE_TOPIC_NAME = "Provided invalid ResponseTopicName";
  String UNSUPPORTED_QOS_OR_RETAIN_HANDLING = "Provided unsupported 'QoS' or 'RetainHandling'";
  String MISSED_REQUIRED_MESSAGE_ID = "'Packet Identifier' must be presented'";
  String NOT_EXPECTED_MESSAGE_ID = "'Packet Identifier' must be zero'";
  String INVALID_SUBSCRIPTION_ID = "Provided invalid 'Subscription Identifier'";
  
  String PROTOCOL_LEVEL_UNSUPPORTED_NO_LOCAL_OPTION = "'No Local' option is not available on this protocol level";
  String PROTOCOL_LEVEL_UNSUPPORTED_RETAIN_AS_PUBLISH_OPTION = "'Retain As Published' option is not available on this protocol level";
  String PROTOCOL_LEVEL_UNSUPPORTED_RETAIN_HANDLING_OPTION = "'Retain Handling' option is not available on this protocol level";

  String UNEXPECTED_FLOW_STATE = "Unexpected flow state:'%s', expected:'%s'";
  String UNEXPECTED_RESPONSE_MESSAGE = "Unexpected response packet:'%s', expected:'%s'";
}
