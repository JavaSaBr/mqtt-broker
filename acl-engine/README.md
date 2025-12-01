## Summary

This module implements an Access Control List rules engine for the MQTT broker.
**_ACL Rules Engine_** stores rules parsed by **_ACL Rules Loader_** and verify user authorization requests against the rules.
It utilizes order-based priority: once a rule matches, its permission (allow or deny) is applied and subsequent rules
are skipped. By default, it denies any incoming request unless it's allowed explicitly.
