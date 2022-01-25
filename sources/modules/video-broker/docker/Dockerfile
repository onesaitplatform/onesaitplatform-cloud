FROM ubuntu:16.04

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="video-broker"	

# logs folder and opencv
RUN mkdir -p /var/log/platform-logs && \
	mkdir -p /usr/local/opencv && \
	mkdir -p /usr/local/app/resources && \
	mkdir ./target
	
ADD resources /usr/local/app/resources
	
# create onesait user/group
RUN addgroup --system onesait --gecos 433 && adduser --gecos 431 --system --ingroup onesait --home /usr/local --disabled-login onesait 

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local

ENV OPENCV_VERSION 3.4.2

RUN apt-get update &&\
    apt-get install -y software-properties-common
    
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y openjdk-8-jdk\
    openjdk-8-jre \
    ant\
    python\
    cmake\
    make\
    wget\
    g++\
    linux-headers-generic\
    libc6 
    
RUN apt-get install -y zip\
	unzip &&\
    wget https://github.com/opencv/opencv/archive/$OPENCV_VERSION.zip \
    && unzip -d /usr/local $OPENCV_VERSION.zip \
    && rm $OPENCV_VERSION.zip


RUN apt-get install -y ffmpeg\
    libavcodec-dev \
    libavformat-dev \
    libavdevice-dev \
    libv4l-dev\
    libpng-dev\
    pkg-config
    
ENV PKG_CONFIG_PATH=/usr/local/x86_64-linux-gnu/pkgconfig:/usr/local/lib/pkg-config

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

RUN cd /usr/local/opencv \
	&& cmake /usr/local/opencv-$OPENCV_VERSION \
	-DBUILD_SHARED_LIBS:BOOL=OFF \
	-DCMAKE_BUILD_TYPE:STRING=Release \
	-DBUILD_opencv_python2:BOOL=OFF \
	-DBUILD_PERF_TESTS:BOOL=OFF \
	-DBUILD_TESTS:BOOL=OFF \
	-DWITH_FFMPEG=ON \
	-DWITH_TBB=ON \
	-DWITH_GTK=ON \
	-DWITH_V4L=ON \
	-DWITH_OPENGL=ON \
	-DWITH_CUBLAS=ON \
	-DWITH_QT=OFF \
	-DCUDA_NVCC_FLAGS="-D_FORCE_INLINES" \
	&& cmake /usr/local/opencv-$OPENCV_VERSION \
	&& make -j \
	&& make install
	
RUN rm -R /usr/local/opencv-$OPENCV_VERSION

ADD *-exec.jar app.jar

RUN chown onesait:onesait app.jar

VOLUME ["/tmp","/var/log/platform-logs"]
  
USER onesait
    
EXPOSE 24000

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
    SERVER_NAME=localhost \
    KAFKAENABLED=true \
    KAFKABROKERS=none \    
    KAFKAHOST=kafka \
    KAFKAPORT=9095 \
    KAFKAUSER=VideoBrokerClient\
    KAFKAPASSWORD=54986d1088d24dd48605a93e85665e1c\
    CONFIGDBSERVERS=configdb:3306 \
    JDBCPROTOCOL="jdbc:mysql:" \
    AUDITGLOBALNOTIFY=true \    
    DBADDPROPS="" \
    LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib64:/usr/local/opencv/lib \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    AUDITGLOBALNOTIFY=true \
    MAXCONN=100 \
    MAXCONNROUTE=100 \
    OP_LOG_LEVEL=INFO  \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201

    
ENTRYPOINT java $JAVA_OPTS -Djava.library.path=/usr/local/opencv/lib -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar
