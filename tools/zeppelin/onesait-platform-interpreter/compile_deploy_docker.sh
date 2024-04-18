#!/bin/sh

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 ZEPPELIN_CONTAINER" >&2
  return
fi

mvn clean package -DskipTests
echo "Compiled"
version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Version: $version"
docker cp ./target/onesait-platform-interpreter-$version-jar-with-dependencies.jar $1:/zeppelin/interpreter/onesaitplatform/onesait-platform-interpreter-$version-jar-with-dependencies.jar
echo "Copied to container $1"
docker restart $1
echo "Restart container $1"
sh installation.sh
echo "End"
