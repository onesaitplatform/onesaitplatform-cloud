server: 
   port: ${PORT}   
   tomcat:
      access_log_enabled: true
      basedir: target/tomcat
   servlet:
      contextPath: ${CONTEXT_PATH}   
spring:
  thymeleaf: ## Thymeleaf Config
      cache: false  
  main.allow-bean-definition-overriding: true
  application.name: ${NAME}
  boot.admin: #Spring Boot Admin Config    
      client.url: http://localhost:18100/monitoring
      client.username: operations
      client.password: changeIt2019!
      client.service-url: http://localhost:${PORT}${CONTEXT_PATH}

management.endpoints.web.exposure.include: "*"
management.endpoint.health.show-details: always  

onesaitplatform:
  iotclient:
    multitenant: false
    urlRestIoTBroker: ${DOMAIN}iot-broker
    sslverify: false
    token: ${DEVICE_TOKEN}
    deviceTemplate: ${DEVICE_TEMPLATE}
    device: ${NAME}
    connectTimeoutInSec: 10
    writeTimeoutInSec: 30
    readTimeoutInSec: 30


security:
  basic:
    enabled: false
  oauth2:
    client:
      clientId: onesaitplatform
      clientSecret: onesaitplatform
      accessTokenUri: ${DOMAIN}oauth-server/oauth/token
      userAuthorizationUri: ${DOMAIN}oauth-server/oauth/authorize
      checkTokenEndpointUrl: ${DOMAIN}oauth-server/oauth/check_token
      useCurrentUri: true
    resource:
      userInfoUri: ${DOMAIN}oauth-server/user


    ## LOGGING CONF
logging:
   path: /tmp/logs 
   file: ${NAME}
   level:
      com.minsait: DEBUG 
      #org.springframework.security: DEBUG

