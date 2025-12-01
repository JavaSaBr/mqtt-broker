This module implements an Access Control List (ACL) rules loader for the MQTT broker.
It enables defining fine-grained permissions for clients — controlling who can publish or subscribe to which topics,
based on client identifiers such as username, client id and IP-address. 

## _ACL Rules Loader_

The loader is a DSL-based ACL configuration parser inspired by HCL format, that allows specifying rules like
`allowPublish`, `denySubscribe`, `allowSubscribe` and `denyPublish`. It supports nesting of `allOf` and `anyOf`
conditions and wildcards with `anyOf()` and `anyone()` keywords. An example of simple ACL file can be found in tests:

https://github.com/JavaSaBr/mqtt-broker/blob/d07808bbd9eaf0264853c832dc83d34e4f591c9d/acl-groovy-dsl/src/test/resources/acl/config/acl.groovy#L3-L50

### Keywords
- `allowPublish` - defines allowing rule for publish operation
- `denySubscribe` - defines denying rule for subscribe operation
- `allowSubscribe` - defines allowing rule for subscribe operation
- `denyPublish` - defines denying rule for publish operation
- `allOf` - conjunction (logical AND) of user matchers, allows only single-matcher members
- `anyOf` - disjunction (logical OR) of user matchers, allows multi-matcher members including `allOf`/`anyOf`
- `username` - defines user matcher based on its user name, supports multi-matchers
- `clientId` - defines user matcher based on its client id, supports multi-matchers
- `ipAddress` - defines user matcher based on its ip address, supports multi-matchers
- `eq` - strict equality matcher
- `regex` - regular expression matcher
- `topicName` - topic name matcher for publish request, supports multi-matchers
- `topicFilter` - topic filter matcher for subscribe request, supports multi-matchers
- `match` - topic filter matcher (respecting topic naming rules and wildcards support)

