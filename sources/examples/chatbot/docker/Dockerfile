FROM ubuntu:16.04

# Install environment for python
RUN apt-get update && apt-get install -y --no-install-recommends python python-dev python-pip gcc g++ build-essential curl wget software-properties-common python-software-properties

# Install JAVA
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
    add-apt-repository -y ppa:webupd8team/java && \
    apt-get update && \
    apt-get install -y oracle-java8-installer && \
    rm -rf /var/cache/oracle-jdk8-installer

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle


RUN wget -O S4C.tar.gz https://seafile.dev.blocksait.com/f/25b17d56f8/?raw=1 && \
    tar xvzf S4C.tar.gz

WORKDIR /app

RUN pip install setuptools wheel && \
    pip install -r requirements.txt && \
    python -m spacy download en_core_web_md && \
    python -m spacy link en_core_web_md en

RUN sed -i 's/wsgi/pywsgi/g' /usr/local/lib/python2.7/dist-packages/rasa_core/channels/rest.py && \
    sed -i 's/button_type="inline"/button_type="vertical"/g' /usr/local/lib/python2.7/dist-packages/rasa_core/channels/telegram.py

RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ADD *-exec.jar /app.jar

EXPOSE 5003 5004 31000

ENV JAVA_OPTS="" \
    SERVER_NAME=localhost

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]
