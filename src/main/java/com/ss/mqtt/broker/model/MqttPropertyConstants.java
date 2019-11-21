package com.ss.mqtt.broker.model;

public interface MqttPropertyConstants {

    QoS MAXIMUM_QOS_DEFAULT = QoS.EXACTLY_ONCE_DELIVERY;

    int MAXIMUM_PROTOCOL_PACKET_SIZE = 256 * 1024 * 1024;
    int MAXIMUM_PACKET_ID = 0xFFFF;

    long SESSION_EXPIRY_INTERVAL_DISABLED = 0;
    long SESSION_EXPIRY_INTERVAL_DEFAULT = 120;
    long SESSION_EXPIRY_INTERVAL_MIN = 0;
    long SESSION_EXPIRY_INTERVAL_INFINITY = 0xFFFFFFFFL;
    long SESSION_EXPIRY_INTERVAL_UNDEFINED = -1;

    int RECEIVE_MAXIMUM_UNDEFINED = -1;
    int RECEIVE_MAXIMUM_MIN = 1;
    int RECEIVE_MAXIMUM_DEFAULT = 10;
    int RECEIVE_MAXIMUM_MAX = 0xFFFF;

    int MAXIMUM_PACKET_SIZE_UNDEFINED = -1;
    int MAXIMUM_PACKET_SIZE_DEFAULT = 1024;
    int MAXIMUM_PACKET_SIZE_MIN = 1;
    int MAXIMUM_PACKET_SIZE_MAX = MAXIMUM_PROTOCOL_PACKET_SIZE;

    boolean PAYLOAD_FORMAT_INDICATOR_DEFAULT = false;

    long MESSAGE_EXPIRY_INTERVAL_UNDEFINED = -1;
    long MESSAGE_EXPIRY_INTERVAL_INFINITY = 0;

    int TOPIC_ALIAS_MAXIMUM_UNDEFINED = -1;
    int TOPIC_ALIAS_MAXIMUM_DISABLED = 0;

    int SERVER_KEEP_ALIVE_UNDEFINED = -1;
    int SERVER_KEEP_ALIVE_DISABLED = 0;
    int SERVER_KEEP_ALIVE_DEFAULT = 0;
    int SERVER_KEEP_ALIVE_MIN = 0;
    int SERVER_KEEP_ALIVE_MAX = 0xFFFF;

    int TOPIC_ALIAS_DEFAULT = 0;
    int TOPIC_ALIAS_MIN = 0;
    int TOPIC_ALIAS_MAX = 0xFFFF;
    int TOPIC_ALIAS_NOT_SET = 0;

    int SUBSCRIPTION_ID_NOT_DEFINED = 0;

    boolean SESSIONS_ENABLED_DEFAULT = true;
    boolean KEEP_ALIVE_ENABLED_DEFAULT = false;
    boolean RETAIN_AVAILABLE_DEFAULT = false;
    boolean WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT = false;
    boolean SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT = false;
    boolean SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT = false;

    int PACKET_ID_FOR_QOS_0 = 0;
    int PACKET_ID_MAX = 0xFFFF;
}
