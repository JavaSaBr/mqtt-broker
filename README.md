# MQTT-Broker

MQTT-Broker is an open source Java based MQTT Broker which implements MQTT v3.1.1 
and v5.0 protocol versions based on core library: https://github.com/JavaSaBr/RLib

### Feature Map
1. MQTT Protocol features:
    - [ ] Connection Request
      - [X] Base handling
      - [ ] CONNECT Variable Header
        - [ ] Clean Start
        - [ ] Will Flag
        - [ ] Will QoS
        - [ ] Will Retain
        - [ ] User Name Flag
        - [ ] Password Flag
        - [ ] Keep Alive
      - [ ] CONNECT Properties
        - [X] Session Expiry Interval
        - [ ] Receive Maximum
        - [ ] Maximum Packet Size
        - [ ] Topic Alias Maximum
        - [ ] Request Problem Information
        - [X] User Property
        - [ ] Authentication Method
        - [ ] Authentication Data
      - [ ] CONNECT Payload
        - [X] Client Identifier
        - [ ] Will Properties
        - [ ] Will Delay Interval
        - [ ] Payload Format Indicator
        - [ ] Message Expiry Interval
        - [ ] Content Type
        - [ ] Response Topic
        - [ ] Correlation Data
        - [ ] User Property
        - [ ] Will Topic
        - [ ] Will Payload
        - [X] User Name
        - [X] Password
    - [ ] Connect acknowledgement
      - [X] Base handling
      - [ ] Session Present
      - [ ] CONNACK Properties
        - [X] Session Expiry Interval
        - [ ] Receive Maximum
        - [X] Maximum QoS
        - [ ] Retain Available
        - [ ] Maximum Packet Size
        - [X] Assigned Client Identifier
        - [ ] Topic Alias Maximum
        - [ ] Reason String
        - [ ] User Property
        - [X] Wildcard Subscription Available
        - [X] Subscription Identifiers Available
        - [X] Shared Subscription Available
        - [ ] Server Keep Alive
        - [ ] Response Information
        - [ ] Server Reference
        - [ ] Authentication Method
        - [ ] Authentication Data
    - [ ] Publish message
      - [X] Base handling
      - [ ] RETAIN
      - [ ] PUBLISH Properties
        - [ ] Message Expiry Interval
        - [X] Payload Format Indicator
        - [ ] Topic Alias
        - [X] Response Topic
        - [X] User Property
        - [ ] Subscription Identifier
        - [X] Content Type
    - [X] Publish acknowledgement
      - [X] Base handling
      - [X] PUBACK Properties
        - [X] Reason String
        - [X] User Property
    - [X] Publish received
      - [X] Base handling
      - [X] PUBREC Properties
        - [X] Reason String
        - [X] User Property
    - [X] Publish release
      - [X] Base handling
      - [X] PUBREL Properties
        - [X] Reason String
        - [X] User Property
    - [X] Publish complete
      - [X] Base handling
      - [X] PUBCOMP Properties
        - [X] Reason String
        - [X] User Property
    - [ ] Subscribe request
      - [X] Base handling
      - [ ] SUBSCRIBE Properties
        - [ ] Subscription Identifier
        - [X] User Property
      - [ ] Subscription Options
        - [ ] Retain Handling
        - [ ] Retain as Published
        - [ ] No Local
    - [X] Subscribe acknowledgement
      - [X] Base handling
      - [X] SUBACK Properties
        - [X] Reason String
        - [X] User Property
    - [X] Unsubscribe request
      - [X] Base handling
      - [X] UNSUBSCRIBE Properties
        - [X] User Property
    - [X] Unsubscribe acknowledgement
      - [X] Base handling
      - [X] UNSUBACK Properties
        - [X] Reason String
        - [X] User Property
    - [ ] PING request
    - [ ] PING response
    - [ ] Disconnect notification
      - [X] Base handling
      - [ ] DISCONNECT Properties
        - [ ] Session Expiry Interval
        - [X] Reason String
        - [X] User Property
        - [ ] Server Reference
    - [ ] Authentication exchange
      - [X] Base handling
      - [ ] AUTH Properties
        - [X] Authentication Method
        - [X] Authentication Data
        - [ ] Reason String
        - [ ] User Property
2. Extra features:
   - [X] ACL Service
     - [X] Disabled ACL
     - [X] Static file based ACL
   - [X] Authentication Service

## Dependencies for building
### java 25+
#### [Temurin JDK](https://adoptium.net/temurin/releases)
### Docker
##### Ubuntu: [installation guide](https://docs.docker.com/install/linux/docker-ce/ubuntu)
##### Windows: [installation guide](https://docs.docker.com/docker-for-windows/install)
##### MacOS: [installation guide](https://docs.docker.com/docker-for-mac/install)
## Build
```bash
./gradlew :application:bootJar
```
## Run
```bash
./gradlew :application:bootRun
```
