: Copy the result target to devops image folder


version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

cp -a ./target/onesait-platform-authenticator-$version.jar ../../../devops/build-deploy/docker/dockerfiles/zeppelin-py3/onesait-platform-authenticator-$version.jar

echo "Copied authenticator version: $version to devops folder zeppelin-py3 "
