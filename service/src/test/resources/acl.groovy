import static javasabr.mqtt.model.acl.Action.*
import static javasabr.mqtt.model.acl.Operator.*
import static javasabr.mqtt.model.acl.Permission.*

acl {
  version 1
}

user("dashboard") {
  groups "admin", "viewer"
}

user "sensor1", {
  password "\$bcrypt:..."
  groups "sensor", "2nd-floor"
}

group("admin") {
  users "dashboard", "root"
}

group("viewer") {
  users "dashboard"
}

group("sensor") {
  users "sensor1", "sensor2"
}

rule("sys_dashboard_sub") {
  permission ALLOW
  action PUBLISH

  clients {
    operator OR
    users "dashboard"
    ipAddresses "10.56.0.3", "120.10.60.60"
  }

  topics "/topic1/#", "/topic2/+/temp"
}

rule("deny_all") {
  permission DENY
  action SUBSCRIBE
  topics "\$SYS/#"
  topics "#"
}
