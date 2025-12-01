## Summary

This module implements an Access Control List rules loader for the MQTT broker.
It enables defining fine-grained permissions for clients — controlling who can publish or subscribe to which topics,
based on client identifiers such as username, client id and IP-address.

**_ACL Rules Loader_** is a DSL-based configuration parser inspired by HCL format, allowing specifying rules like
`allowPublish`, `denySubscribe`, `allowSubscribe` and `denyPublish`, supporting nesting of `allOf` and `anyOf`
conditions and wildcards with `anyOf()` and `anyone()` keywords. An example of simple ACL file can be found in tests:

https://github.com/JavaSaBr/mqtt-broker/blob/d07808bbd9eaf0264853c832dc83d34e4f591c9d/acl-groovy-dsl/src/test/resources/acl/config/acl.groovy#L3-L50

### Domain Specific Language

#### Rules
- `allowPublish` - defines allowing rule for publish operation
- `denySubscribe` - defines denying rule for subscribe operation
- `allowSubscribe` - defines allowing rule for subscribe operation
- `denyPublish` - defines denying rule for publish operation
#### Compose Conditions
- `allOf` - conjunction condition (logical AND) of user conditions, allows only single-matcher members
- `anyOf` - disjunction condition (logical OR) of other conditions, allows multi-matcher members including `allOf`/`anyOf`
#### User Conditions
- `username` - defines user matcher based on its username, supports multi-matchers
- `clientId` - defines user matcher based on its client id, supports multi-matchers
- `ipAddress` - defines user matcher based on its ip address, supports multi-matchers
#### Topic Conditions
- `topicName` - topic name condition for publish request, supports multi-matchers
- `topicFilter` - topic filter condition for subscribe request, supports multi-matchers
#### Value Matchers
- `eq` - strict equality matcher
- `regex` - regular expression matcher
- `match` - topic filter matcher (respecting topic naming rules and wildcards support)

#### Constraints

- Maximum number of rules 1000
- Rule can contain only one compose condition
- Publish rule can contain several topic names
- Subscribe rule can contain several topic filters
- AllOf condition can contain only single client id, username or IP-address matchers
- AnyOf condition can contain multiple client id, username, IP-address matchers, or other AllOf and AnyOf conditions
- Username, Client ID and IP address conditions accept one or many regular expression or strict equality matchers
- Topic name condition accepts one or many strict equality matchers
- Topic filter condition accepts one or many topic filter matchers
- Strict equality matcher compares two string values
- Regular expression matcher verifies if incoming value match regex
- Topic filter matcher verifies if incoming topic match subscription topic filter respecting `#` and `+` wildcards
