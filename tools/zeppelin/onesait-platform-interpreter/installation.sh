: Copy the result target to devops image folder

version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
cp -a ./target/onesait-platform-interpreter-$version-jar-with-dependencies.jar ../../../devops/build-deploy/docker/dockerfiles/zeppelin-py3/onesait-platform-interpreter-$version.jar
echo "Copied interpreter version: $version to devops folder zeppelin-py3"
