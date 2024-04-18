: Change docker container ID in both places with your dataflow docker contaniner id

set CONTAINER_ID=%1
call mvn clean package -DskipTests
call docker cp .\target\onesait-platform-interpreter-1.0.0.v0.10.1-jar-with-dependencies.jar %CONTAINER_ID%:/zeppelin/interpreter/onesait-platform-interpreter-1.0.0.v0.10.1-jar-with-dependencies.jar
call docker restart %CONTAINER_ID%
