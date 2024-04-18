server:
   port: ${serverPort?c}
   contextPath: ${serverContextPath}
   ip: localhost

spring:
   application.name: ${applicationName}

api.key: ${apiKey}

device:
   id: ${deviceId}
   rest:
     local:
        schema: ${deviceRestLocalSchema}
        network:
           interface: ${deviceLocalInterface}
           ipv6: ${deviceIpv6?c}
     basepath: ${serverContextPath}
   register.fail.retry.seconds: 60
   ping.interval.seconds: 10
   logic.main.loop.delay.seconds: 5

onesaitplatform.digitaltwin.broker.rest: ${onesaitplatformBrokerEndpoint}
