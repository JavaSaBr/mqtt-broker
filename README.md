# MQTT-Broker

MQTT-Broker is an open-source Java MQTT broker that targets MQTT **v3.1.1** and **v5.0** and is built on top of the [RLib](https://github.com/JavaSaBr/RLib) ecosystem.

## Current capabilities

- Standalone Spring Boot application and embeddable broker module
- MQTT 3.1.1 and MQTT 5 protocol support with an explicit feature-progress map
- Authentication support, including file-backed credentials
- ACL support, including disabled mode and static file/Groovy DSL based configuration
- Stateful publish lifecycle work for incoming storage, retained handling, sender cleanup, and QoS tracking

## Quick start

### Requirements

- **Java 25**
- **Gradle Wrapper** (`./gradlew`)
- Network access to GitLab Maven packages for RLib dependencies

### Run the broker

```bash
./gradlew :application:bootRun
```

Default external MQTT listener:

- host: `localhost`
- port: `1883`

### Build an executable jar

```bash
./gradlew :application:bootJar
```

Output:

```text
application/build/libs/application-0.0.1.jar
```

### Default local credentials

The repository includes a simple credentials file for local runs:

```text
application/src/main/resources/credentials
```

Format:

```text
user=password
```

## Project structure

| Module | Purpose |
| --- | --- |
| `base` | Shared base utilities and Jackson/debug support |
| `model` | MQTT domain model: topics, sessions, publishes, QoS, subscriptions |
| `network` | MQTT network protocol layer, message encoding/decoding, connection support |
| `core-service` | Main broker services: routing, subscriptions, publish flow, sessions |
| `application` | Standalone Spring Boot application |
| `embedded` | Embeddable broker module for use inside another application |
| `acl-engine` | ACL rule evaluation engine |
| `acl-groovy-dsl` | Groovy DSL for ACL configuration |
| `acl-service` | Spring wiring for ACL services |
| `authentication-*` | Authentication API, providers, and service wiring |
| `credentials-source-*` | Credential source implementations |
| `test-support` | Shared test utilities and fixtures |
| `test-coverage` | Aggregated JaCoCo coverage reporting |

## Configuration

Important runtime properties include:

| Property | Default | Meaning |
| --- | --- | --- |
| `mqtt.external.network.host` | `localhost` | External MQTT bind host |
| `mqtt.external.network.port` | `1883` | External MQTT bind port |
| `authentication.provider.anonymous.enabled` | `false` | Allow anonymous connections |
| `authentication.credentials-source.file.enabled` | unset | Enable file-backed credentials source |
| `authentication.credentials-source.file.path` | unset | Credentials file location |
| `mqtt.external.connection.sessions.enabled` | framework default | Enable persistent sessions |
| `mqtt.external.connection.retain.available` | `false` | Advertise retained publish support to clients |

Current repository defaults in `application/src/main/resources/application.properties` keep anonymous authentication disabled.

## Build and test

Compile all test classes:

```bash
./gradlew testClasses
```

Run the full test suite:

```bash
./gradlew test
```

Run a single spec:

```bash
./gradlew :core-service:test --tests 'javasabr.mqtt.service.publish.processor.Qos2IncomingPublishProcessorTest'
```

Generate coverage reports:

```bash
./gradlew jacocoTestReport test-coverage:testCodeCoverageReport
```

## Publish flow overview

```text
client PUBLISH
   |
   v
PublishMqttInMessageHandler
   |
   +--> PublishDataStorage.store(...)
   |
   +--> IncomingPublishStorage.store(...)
            |
            v
      QoS processor
         |
         +--> validate / track session state
         +--> send protocol feedback
         +--> dispatch to subscribers
         +--> retained handling / scheduled cleanup when needed
```

## Embedding

Use the `embedded` module when the broker should run as part of another Java application instead of as the standalone Spring Boot application.

## Known limitations

- The feature map below is the authoritative protocol progress tracker and includes partially implemented MQTT 5 areas.
- Several protocol areas are intentionally incomplete and marked in the feature map rather than treated as defects.
- The standalone build requires external dependency resolution from GitLab-hosted RLib packages.

## Development notes

- Always use `./gradlew` so the repository stays on the expected Gradle version.
- Java preview features are required by the build and test setup.
- Docker is **not required** for the current build/test workflow.

## Feature Map

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
