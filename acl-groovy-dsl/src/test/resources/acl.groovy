allowPublish {
  anyOf {
    userName eq("sensor1"), regex("/sensor10\$/")
    clientId eq("clientId1"), regex("/^cliend/")
    ipAddress eq("10.56.0.3"), eq("127.0.0.1")
    allOf {
      userName eq("sensor2")
      clientId eq("clientId2")
      ipAddress eq("10.56.0.3")
    }
  }
  topicName exact("/topic1"), exact("/topic2/temp")
}

denySubscribe {
  allOf {
    userName eq("sensor2")
    userName regex("/sensor11\$/")
    clientId eq("clientId2")
    clientId regex("/^cliend1/")
    ipAddress eq("10.56.0.3")
    ipAddress eq("127.0.0.1")
  }
  topicFilter match("/topic1/#")
  topicFilter match("/topic2/+/temp")
}
