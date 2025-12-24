allowPublish {
  anyOf {
    clientId startWith("device_")
  }
  topicName eq('/notification/service/cluster/service1'), 
      eq('/notification/service/cluster/service2')
}

allowSubscribe {
  anyOf {
    clientId startWith("device_")
  }
  topicFilter eq('notification/device/${clientId}'),
      eq('/broadcast/devices')
}

allowPublish {
  anyOf {
    clientId startWith("service_")
  }
  topicName eq('notification/device/+'),
      eq('/broadcast/devices')
}

allowSubscribe {
  anyOf {
    clientId startWith("service_")
  }
  topicFilter eq('/notification/service/cluster/service1'), 
      eq('/notification/service/cluster/service2'),
      eq('/notification/service/node/${clientId}')
}

denyPublish {
  anyOf()
  topicName anyone()
}

denySubscribe {
  anyOf()
  topicFilter anyone()
}
