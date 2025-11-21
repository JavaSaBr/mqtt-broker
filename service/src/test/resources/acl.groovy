rule("sys_dashboard_sub") {
  permission ALLOW
  action PUBLISH
  clients {
    operator OR
    username "sensor1", "sensor10", "/^sensor1/", "/sensor10\$/"
    clientId "sensor1", "sensor10", "/^sensor1/", "/sensor10\$/"
    clientAttr "attr_name1", "attr_value1", "attr_name2", "/attr_value\$/"
    ipaddr "10.56.0.3", "127.0.0.1"
  }
  topics "/topic1/#", "/topic2/+/temp"
}

rule("deny_subscribe_all") {
  permission DENY
  action SUBSCRIBE
  topics "\$SYS/#"
  topics "#"
}

rule("allow_all") {
  permission ALLOW
  action ALL
}