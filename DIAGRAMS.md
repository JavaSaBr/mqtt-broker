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
