package acl.config

allowPublish {
  users {
    clientId eq("clientId2")
    anyOf {
      userName eq("sensor2")
      allOf {
        clientId eq("clientId3")
        userName eq("sensor3")
      }
    }
    allOf {
      clientId eq("clientId4")
      ipAddress anyValue()
    }
  }
  topics {
    eq("/topic1")
    match("/topic2/temp")
  }
}

allowSubscribe {
  
}

/*
allowPublish {
  users {
    allOf {
      clientId eq("clientId2")
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
*/
