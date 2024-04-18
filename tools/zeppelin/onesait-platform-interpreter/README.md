# Onesait Platform Interpreter for Apache Zeppelin 0.8.0

## Interpreter generation
1 - To generate jar use the following command in this project:

```sh
$ mvn clean install
```

2 - The library zeppelin-interpreter (org.apache.zeppelin) is required for compilation

3 - Create a folder called in interpreter folder (in zeppelin instalation directory) called “onesaitplatform" and paste the generated jar "zeppelin-onesait-platform-V-V-V.jar" inside

4 - Edit the file zeppelin_site.xml and include in the property “zeppelin.interpreters” a new entry (comma separeted) called org.apache.zeppelin.onesaitplatform.iotbroker.OnesaitPlatformInterpreter

5 - Restart Zeppelin

## Interpreter deploy
To deploy the interpreter in a new zeppelin image, it is included the script installation.sh which copy the resulting .jar file to the deploy docker folder beside dockerfile.

## Interpreter testing
For testing in the onesaitplatform/zeppelin docker image, it is included the file compile_and_deploy_docker.sh.
This script compiles and copies a new interpreter into docker container running. Finally, it restarts the container.

## Interpreter use
This interpreter group implements the onesait platform functionality. It can be used from zeppelin notebooks with tag %onesaitplatform.

The interpreters in the onesaitplatform group are:

- %onesaitplatform: iot-broker rest client to exchange data with the platform.

- %onesaitplatform.apimanager: api-manager client to manage and call APIs REST of the platform

## More info
For more information, please visit [Onesait Platform notebooks documentation](https://onesaitplatform.atlassian.net/wiki/spaces/OP/pages/49807646/Notebooks+Guides)
