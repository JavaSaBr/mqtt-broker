## Data Flow Diagram
### High-Level
```mermaid
graph LR
    Client((MQTT Client)) -- TCP Connection --> Network[Network Layer]
    Network -- Raw MQTT Packets --> Core[Core Service / Packet Handlers]
    Core -- Credentials --> Auth[Authentication Service]
    Auth -- Success/Fail --> Core
    Core -- Permission Check --> ACL[ACL Service]
    ACL -- Granted/Denied --> Core
    Core -- Publish Message --> Pub[Publishing Service]
    Pub -- Routed Message --> Network
```

### Mid-Level
```mermaid
graph TD
    Client((MQTT Client)) <--> Conn[MqttConnection]
    Conn <--> NetUser[Network User]
    NetUser --> Handlers[Packet Handlers]

    subgraph Security
        Handlers -- Credentials --> AuthSvc[Auth Service]
        AuthSvc -- Provider --> AuthProv[Auth Provider]
        AuthProv -- JDBC --> DBSrc[(DB Source)]
        AuthProv -- Read --> FileSrc[(File Source)]
        Handlers -- Topic+User --> ACLSvc[ACL Service]
        ACLSvc -- Evaluate --> ACLPol{ACL Policy}
    end

    subgraph Core Services
        Handlers -- Raw Topic --> TopicSvc[Topic Service]
        TopicSvc -- Filter/Name --> Handlers
        Handlers -- Subscribe --> SubSvc[Subscription Service]
        SubSvc -- matches --> SubTree[Subscriber Tree]
        Handlers -- findRetained --> RetainSvc[Retain Service]
        RetainSvc -- matches --> RetainTree[Retain Tree]
    end

    subgraph Publishing Engine
        Handlers -- Publish --> PubRouter[Publish Router]
        PubRouter -- QoS 0/1/2 --> QoSProcs[QoS Processors]
        QoSProcs -- Find --> SubSvc
        QoSProcs -- Dispatch --> PubDisp[Publish Dispatcher]
        PubDisp -- Store --> RetainSvc
        RetainSvc -- update --> RetainTree
        PubDisp -- send --> NetUser
        QoSProcs -- QoS > 0 --> PDS[(Publish Data Storage)]
    end
```

## Sequence Diagrams
### Client Connection & Publish
```mermaid
sequenceDiagram
    participant C as MQTT Client
    participant N as Network Layer
    participant CS as ConnectionService
    participant AS as AuthenticationService
    participant PM as Protocol Handlers
    participant ACL as AuthorizationService

    C->>N: TCP Connection
    N->>CS: New Connection Established
    CS->>PM: Await CONNECT Packet
    C->>PM: Send CONNECT
    PM->>AS: Validate Credentials
    AS-->>PM: Auth Result
    PM-->>C: CONNACK
    C->>PM: Send PUBLISH
    PM->>ACL: Check Publish Permission
    ACL-->>PM: Permission Result
    PM->>PM: Process PUBLISH
```

### Subscription Flow
```mermaid
sequenceDiagram
    participant C as MQTT Client
    participant Conn as MqttConnection
    participant H as SubscribeHandler
    participant S as SubscriptionService
    participant R as RetainService
    participant D as PublishDispatcher

    C->>Conn: SUBSCRIBE
    Conn->>H: process message
    H->>S: subscribe(...)
    S-->>H: results
    H-->>C: SUBACK
    H->>R: find retained
    R-->>H: messages
    H->>D: dispatch
    D-->>C: PUBLISH (retained)
```

### QoS 1 & 2 Handshake
```mermaid
sequenceDiagram
    participant C as Client
    participant B as Broker
    
    rect rgb(240, 240, 240)
    Note over C, B: QoS 1: At Least Once
    C->>B: PUBLISH (QoS 1)
    B-->>C: PUBACK
    end

    rect rgb(220, 230, 240)
    Note over C, B: QoS 2: Exactly Once
    C->>B: PUBLISH (QoS 2)
    B-->>C: PUBREC
    C->>B: PUBREL
    B-->>C: PUBCOMP
    end
```

### Session & Message Tracking
Manages the state of QoS 1/2 messages, ensuring "Exactly Once" delivery and handling Packet ID lifecycle.
```mermaid
sequenceDiagram
    participant U as Network User
    participant P as ProcessingPublishes
    participant R as Retryer
    participant T as Message Tacker

    Note over U, T: QoS 1/2 Outbound
    U->>P: register(publish)
    P->>R: start retry loop
    R->>U: send (DUP=1)
    U->>P: receive ACK
    P->>R: stop
    P->>T: remove(messageId)
```

### Disconnect & Will Message (LWT)
```mermaid
sequenceDiagram
    participant C as Client
    participant B as Broker
    participant S as Subscribers

    Note over C, B: Abnormal Disconnect
    C-x B: Connection Lost / Timeout
    B->>S: PUBLISH Will Message
```

### Unsubscribe & Ping
```mermaid
sequenceDiagram
    participant C as Client
    participant B as Broker

    rect rgb(240, 240, 240)
    Note over C, B: Unsubscribe
    C->>B: UNSUBSCRIBE
    B-->>C: UNSUBACK
    end

    rect rgb(220, 230, 240)
    Note over C, B: Heartbeat
    C->>B: PINGREQ
    B-->>C: PINGRESP
    end
```

## UML Activity Diagram: Message Processing
```mermaid
graph TD
    A[Receive Packet] --> B{Packet Type?}
    B -->|CONNECT| C[Authenticate]
    B -->|PUBLISH| D[Authorize]
    B -->|SUBSCRIBE| E[Update Subscriptions]
    C --> F{Auth Success?}
    D --> G{Authorized?}
    F -->|No| H[Close Connection]
    G -->|No| I[Error Log / Disconnect]
    F -->|Yes| J[Create Session]
    G -->|Yes| K[Deliver Message]
```

## State Machine: Client Session
```mermaid
stateDiagram-v2
    [*] --> Disconnected
    Disconnected --> Connecting: TCP Connect
    Connecting --> AuthInProgress: Send CONNECT
    AuthInProgress --> Connected: Auth Success
    AuthInProgress --> Disconnected: Auth Fail
    Connected --> Disconnected: Close Connection / Timeout
    Connected --> Connected: Process Packets
```

## Component Diagram
Shows the modular structure of the broker and dependencies between modules.
```mermaid
graph TD
    App[Application] --> Core[Core Service]
    App --> ACLS[ACL Service]
    App --> ACLG[ACL Groovy DSL]
    App --> AuthS[Authentication Service]

    Core --> Net[Network Layer]
    Core --> ACLE[ACL Engine]
    Core --> AuthA[Authentication API]
    Core --> IPS[Incoming Publish Storage]

    ACLS --> ACLE
    ACLS --> Core

    AuthS --> AuthA
    AuthS --> Model[Model]
    AuthS --> Base[Base]

    BAP[Basic Auth Provider] -.-> AuthA
    FCS[File Credentials Source] -.-> AuthA
    DBCS[DB Credentials Source] -.-> AuthA
```

## Deployment Diagram
Typical deployment structure of the MQTT Broker.
```mermaid
graph TB
    subgraph Client Device
        App[MQTT Client App]
    end
    subgraph Broker Server
        subgraph MQTT Broker Application
            Core[Core Services]
            Net[Network Layer]
            Auth[ACL/Auth Services]
        end
        DB[(PostgreSQL)]
        FS[Config Files]
    end

    App -- "MQTT (1883/8883)" --> Net
    Auth -- JDBC --> DB
    Auth -- File I/O --> FS
```

## Incoming Publish Storage
This service provides a centralized global store for incoming `PUBLISH` messages, ensuring safe multi-consumer tracking and lifecycle management (especially for retained messages).

### Message Processing Data Flow
```mermaid
graph TD
    Handler[PublishMqttInMessageHandler] -- "1. store" --> Storage[IncomingPublishStorage]
    Handler -- "2. route by QoS" --> Router[IncomingPublishRouter]
    Router -- QoS 0 --> Qos0[Qos0IncomingPublishProcessor]
    Router -- QoS 1 --> Qos1[Qos1IncomingPublishProcessor]
    Router -- QoS 2 --> Qos2[Qos2IncomingPublishProcessor]

    subgraph QoS 0 & 1 - Immediate Dispatch
        Qos0 -- "3. processImpl → dispatch" --> Dispatch[dispatchToSubscriber]
        Qos1 -- "3. processImpl → dispatch" --> Dispatch
    end

    subgraph QoS 2 - Two-Phase
        Qos2 -- "processImpl → send PUBREC" --> SendPUBREC[Send PUBREC + Register Callback]
        SendPUBREC -. "PUBREL arrives" .-> HandlePUBREL[handleReceivedTrackableMessage]
        HandlePUBREL -- "3. dispatch" --> Dispatch
    end

    subgraph Dispatch & Consumer Counting
        Dispatch -- "4. +1 dispatch marker" --> Storage
        Dispatch -- "5. +1 retain marker" --> Storage
        Dispatch -- "6. retain" --> Retain[RetainPublishService]
        Retain -- "prevRetained? -1" --> Storage
        Dispatch -- "7. findSubscribers" --> SubService[SubscriptionService]
        SubService -- "+N per matched subscriber" --> Storage
        Dispatch -- "8. checkSubscriber, skip? -N skipped" --> Storage
        Dispatch -- "9. -1 dispatch marker released" --> Storage
    end

    Dispatch -- "10. per subscriber" --> Sender[SubscriberPublishSender]

    subgraph Sender - Decrement on Completion
        Sender -- "QoS0: -1 after async send\n QoS1: -1 on PUBACK\n QoS2: -1 on PUBCOMP\n early exit: -1" --> Storage
    end

    Storage -- "14. counter == 0 and not retained → auto-remove" --> Cleanup[Cleanup]
```

### Storage Lifecycle & Consumer Counting
This diagram illustrates the lifecycle of a `PUBLISH` message and how the reference counter in `IncomingPublishStorage` is managed during dispatch.

```mermaid
sequenceDiagram
    participant P as IncomingPublishProcessor
    participant IPS as IncomingPublishStorage
    participant R as RetainPublishService
    participant S as SubscriptionService
    participant D as PublishDispatcher
    participant SS as SubscriberPublishSender

    Note over P: dispatchToSubscriber(publish)
    P->>IPS: increaseConsumerCount(publish, 1) [dispatch marker]

    alt publish.retained() and payload not empty
        P->>IPS: increaseConsumerCount(publish, 1) [retain hold]
        P->>R: retain(publish)
        R-->>P: prevRetained (or null)
        alt prevRetained != null
            P->>IPS: decreaseConsumerCount(prevRetained, 1) [release old retain]
        end
    end

    P->>S: findSubscribers(topicName)
    S-->>P: subscribers (N matched)

    alt N == 0
        P->>IPS: decreaseConsumerCount(publish, 1) [release dispatch marker]
    else N > 0
        P->>IPS: increaseConsumerCount(publish, N) [one per subscriber]
        loop For each subscriber
            P->>P: checkSubscriber(publish, subscriber)
            alt check passes
                P->>D: dispatchToSubscriber(publish, subscriber)
                D->>SS: sendToSubscriber(incomingPublish, subscriber)
            else check fails (skipped)
                Note over P: skipped++
            end
        end
        alt skipped > 0
            P->>IPS: decreaseConsumerCount(publish, skipped) [release skipped holds]
        end
        alt matched > 0
            P->>IPS: decreaseConsumerCount(publish, 1) [release dispatch marker]
        else matched == 0
            P->>IPS: decreaseConsumerCount(publish, 1) [release dispatch marker]
        end
    end

    Note over SS: Per-subscriber delivery completion
    alt QoS 0
        SS->>IPS: decreaseConsumerCount(outgoingPublish.source(), 1) [async send complete]
    else QoS 1
        SS->>IPS: decreaseConsumerCount(outgoingPublish.source(), 1) [PUBACK received]
    else QoS 2
        Note over SS: PUBLISH -> PUBREC(success) -> PUBREL -> PUBCOMP
        SS->>IPS: decreaseConsumerCount(outgoingPublish.source(), 1) [PUBCOMP received]
    end

    Note over IPS: When counter reaches 0 and not retained → auto-remove
```

### QoS 2 Inbound Two-Phase Processing
QoS 2 splits processing into two phases: receiving the PUBLISH (sending PUBREC) and processing on PUBREL.

```mermaid
sequenceDiagram
    participant C as Publisher Client
    participant Q2 as Qos2IncomingPublishProcessor
    participant IPS as IncomingPublishStorage
    participant T as InMessageTracker
    participant PP as ProcessingPublishes
    participant D as dispatchToSubscriber

    C->>Q2: PUBLISH (QoS 2)
    Q2->>T: register(messageId, PUBLISH)
    Q2->>PP: register(publish, callback, retryer)
    Q2-->>C: PUBREC (success)
    Note over Q2,IPS: Publish sits in storage with counter = 0

    C->>Q2: PUBREL
    Q2->>T: update(messageId, PUBLISH_COMPLETE)
    Q2->>D: dispatchToSubscriber(publish)
    Note over D: Full consumer counting lifecycle runs here
    D-->>Q2: dispatch complete
    Q2-->>C: PUBCOMP
```

### Sender Error & Early-Exit Paths
Every terminal path in the sender must decrement the consumer count. This diagram shows the non-happy paths.

```mermaid
sequenceDiagram
    participant P as PublishDispatcher
    participant S as AbstractSubscriberPublishSender
    participant IPS as IncomingPublishStorage

    P->>S: sendToSubscriber(incomingPublish, subscriber)

    alt wrong user type
        S->>IPS: decreaseConsumerCount(incomingPublish, 1)
    else session is null
        S->>IPS: decreaseConsumerCount(incomingPublish, 1)
    else buildOutgoingPublish returns null
        S->>IPS: decreaseConsumerCount(incomingPublish, 1)
    else QoS 1 unexpected flow state
        S->>IPS: decreaseConsumerCount(outgoingPublish.source(), 1)
        S->>S: disconnect subscriber
    else QoS 2 PUBREC with error
        S->>IPS: decreaseConsumerCount(outgoingPublish.source(), 1)
        Note over S: cancel flow, remove tracker
    else QoS 2 unknown tracked meta
        S->>IPS: decreaseConsumerCount(outgoingPublish.source(), 1)
    end
```
