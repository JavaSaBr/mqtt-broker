acl {
  version 1
}

user("dashboard") {
  groups "admins"
}

user("sensor1") {
  password "\$bcrypt:..."
  groups "sensors"
}

group("admins") {
  users "dashboard"
}

group("sensors") {
  users "sensor1"
}

rule("sys_dashboard_sub") {
  priority 100
  effect "allow"
  event "sub"

  clients {
    users "dashboard"
  }

  topics "\$SYS/#"
}

rule("deny_all") {
  priority 0
  effect "deny"
  event "sub"
  topics "\$SYS/#"
  topics "#"
}
