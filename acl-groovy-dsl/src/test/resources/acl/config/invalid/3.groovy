package acl.config.invalid

allowPublish {
  anyOf {
    userName eq("sensor1"), regex("sensor10\$")
    clientId eq("clientId1"), regex("^cliend")
    ipAddress eq("10.56.0.3"), eq("127.0.0.1")
    allOf {
      userName eq("sensor2"), eq("sensor2")
      clientId eq("clientId2")
      ipAddress eq("10.56.0.3")
    }
  }
  topicName eq("/topic1"), eq("/topic2/temp")
}
