acl {
  version = 1
}

#  Users / Groups
user "dashboard" {
  groups = ["admins"]
}

user "sensor1" {
  password = "$bcrypt:..."
  groups   = ["sensors"]
}

group "admins" {
  users = ["dashboard"]
}

group "sensors" {
  users = ["sensor1"]
}

#  Rules
rule "sys_dashboard_sub" {
  priority = 100
  effect   = "allow"
  event    = "sub"

  clients {
    users = ["dashboard"]
  }

  topics = ["$SYS/#"]
}

rule "sys_dashboard_sub_2" {
  priority = 100
  effect   = "allow"

  event = "sub"
  clients {
    users = ["dashboard"]
  }

  topics = ["$SYS/#"]
}

rule "pub_temperature" {
  priority = 50
  effect   = "allow"
  event    = "pub"

  topics = ["/smarthome/%c/temperature"]
}

rule "ip_pubsub" {
  priority = 10
  effect   = "allow"
  event    = "pubsub"

  clients {
    ipAddresses = ["10.211.55.10"]
  }

  topics = ["$SYS/#", "#"]
}

rule "deny_all_sys_and_hash_sub" {
  priority = 0
  effect   = "deny"
  event    = "sub"

  topics = ["$SYS/#", "#"]
}
