## _ACL Rules Engine_

This module implements an Access Control List (ACL) rules engine for the MQTT broker.
It stores ACL rules parsed by ACL Rules Loader and verify user authorization requests against the rules.
It utilizes order-based priority: once a rule matches, its permission (allow or deny) is applied and subsequent rules
are skipped. By default, it denies any incoming request unless it's allowed explicitly.
