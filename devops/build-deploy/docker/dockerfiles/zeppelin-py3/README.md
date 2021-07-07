**Apache Zeppelin 0.8 Dockerized**

## Compile sources with profiles:

- mvn clean package -Pbuild-distr -Pspark-2.0 -Pscala-2.11

## Python libraries installed inside image:

apt-get -y install python3-pip && \
python3 -m pip install pandas && \
python3 -m pip install scipy && \
python3 -m pip install sklearn && \
python3 -m pip install matplotlib && \
apt-get install python3-tk && \
python3 -m pip install xgboost

## How to build

- https://zeppelin.apache.org/docs/0.8.0-SNAPSHOT/setup/basics/how_to_build.html

- Copy distributable in this folder to build the image