allowPublish {
  anyClient {
    username eq("sensor1"), regex("/sensor10\$/")
    clientId eq("clientId1"), regex("/^cliend/")
    clientAttr(
        attr_name1: "attr_value1",
        attr_name2: "/attr_value\$/"
    )
    ipaddr eq("10.56.0.3"), eq("127.0.0.1")
  }
  topicName "/topic1", "/topic2/temp"
}

denySubscribe {
  allClients {
    username eq("sensor2")
    username regex("/sensor11\$/")
    clientId eq("clientId2")
    clientId regex("/^cliend1/")
    clientAttr(
        attr_name1: "attr_value1",
        attr_name2: "/attr_value\$/"
    )
    ipaddr eq("10.56.0.3")
    ipaddr eq("127.0.0.1")
  }
  topicFilter "/topic1/#"
  topicFilter "/topic2/+/temp"
}
