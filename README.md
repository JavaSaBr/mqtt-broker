# MQTT-Broker

MQTT-Broker is an open source Java based MQTT Broker which implements MQTT v3.1.1 
and v5.0 protocol versions based on core library: https://github.com/JavaSaBr/RLib

# Feature Map
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
        - [ ] Session Expiry Interval
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
      - [ ] Session Present
      - [ ] CONNACK Properties
        - [ ] Session Expiry Interval
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
      - [ ] Correlation Data
      - [ ] Correlation Data
      - [ ] Correlation Data
      - [ ] Correlation Data
      - [ ] Correlation Data
      - [ ] Correlation Data
      - [ ] Correlation Data
      - [ ] Correlation Data
      - [ ] Correlation Data
2. 
- [ ] Feature 2

## Dependencies for building
### java 25+
#### [Temurin JDK](https://adoptium.net/temurin/releases)
### Docker
##### Ubuntu: [installation guide](https://docs.docker.com/install/linux/docker-ce/ubuntu)
##### Windows: [installation guide](https://docs.docker.com/docker-for-windows/install)
##### MacOS: [installation guide](https://docs.docker.com/docker-for-mac/install)
## Build
```bash
./gradlew buildSingleArtifact 
```
## Run
```bash
./gradlew bootRun
```
