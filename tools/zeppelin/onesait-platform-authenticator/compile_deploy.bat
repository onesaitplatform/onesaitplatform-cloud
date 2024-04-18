SET dockerContainer=378d89ddbcbf
CALL mvn clean install
CALL docker cp .\docker\conf %dockerContainer%:/zeppelin/
CALL docker cp .\target\authenticator-1.0.0.jar %dockerContainer%:/zeppelin/lib/authenticator-1.0.0.jar
ECHO ---- Restarting container ---- 
CALL docker restart %dockerContainer%