## 1. Spring Boot Admin Server

http://www.baeldung.com/spring-boot-admin

* mvn clean install
* mvn spring-boot:run
* starts on port 18090
* login with admin/admin at localhost:18090/monitoring-ui
* to activate mail notifications uncomment the starter mail dependency
and the mail configuration from application.properties
* add some real credentials if you want the app to send emails
* to activate Hipchat notifications proceed same as for email

