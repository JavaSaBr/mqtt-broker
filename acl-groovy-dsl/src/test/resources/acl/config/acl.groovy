package acl.config

allowPublish {
  anyOf {
    userName eq("sensor1"), regex("sensor10\$")
    clientId eq("clientId1"), regex("^cliend")
    ipAddress eq("10.56.0.3"), eq("127.0.0.1")
    anyOf {
      userName eq("sensor2")
      clientId eq("clientId2")
      ipAddress eq("10.56.0.3")
    }
    allOf {
      userName eq("sensor2")
      clientId eq("clientId2")
      ipAddress eq("10.56.0.3")
    }
  }
  topicName eq("/topic1"), eq("/topic2/temp")
}

denySubscribe {
  allOf {
    userName eq("sensor2")
    clientId eq("clientId2")
    ipAddress eq("10.56.0.3")
  }
  topicFilter match("/topic1/#")
  topicFilter match("/topic2/+/temp")
}

allowSubscribe {
  allOf {
    userName eq("sensor2")
    clientId eq("clientId2")
    ipAddress eq("10.56.0.3")
  }
  topicFilter match("/topic1/#")
  topicFilter match("/topic2/+/temp")
}

denyPublish {
  anyOf()
  topicName anyone()
}

denySubscribe {
  anyOf()
  topicFilter anyone()
}
