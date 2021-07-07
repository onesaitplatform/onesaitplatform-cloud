# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM ubuntu:16.04

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="notebooks"

# `Z_VERSION` will be updated by `dev/change_zeppelin_version.sh`
ENV Z_VERSION="0.8.2"
ENV LOG_TAG="[ZEPPELIN_${Z_VERSION}]:" \
    Z_HOME="/zeppelin" \
    LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8

RUN echo "$LOG_TAG update and install basic packages" && \
    apt-get -y update && \
    apt-get install -y locales && \
    locale-gen $LANG && \
    apt-get install -y software-properties-common && \
    apt -y autoclean && \
    apt -y dist-upgrade && \
    apt-get install -y build-essential

RUN echo "$LOG_TAG install tini related packages" && \
    apt-get install -y wget curl grep sed dpkg && \
    TINI_VERSION=`curl https://github.com/krallin/tini/releases/latest | grep -o "/v.*\"" | sed 's:^..\(.*\).$:\1:'` && \
    curl -L "https://github.com/krallin/tini/releases/download/v${TINI_VERSION}/tini_${TINI_VERSION}.deb" > tini.deb && \
    dpkg -i tini.deb && \
    rm tini.deb

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
RUN echo "$LOG_TAG Install java8" && \
    apt-get -y update && \
    apt-get install -y openjdk-8-jdk && \
    rm -rf /var/lib/apt/lists/*

# should install conda first before numpy, matploylib since pip and python will be installed by conda
RUN echo "$LOG_TAG Install miniconda3 related packages" && \
    apt-get -y update && \
    apt-get install -y bzip2 ca-certificates \
    libglib2.0-0 libgit2-dev libxext6 libsm6 libxrender1 \
    git mercurial subversion && \
    echo 'export PATH=/opt/conda/bin:$PATH' > /etc/profile.d/conda.sh && \
    wget --quiet https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh -O ~/miniconda.sh && \
    /bin/bash ~/miniconda.sh -b -p /opt/conda && \
    rm ~/miniconda.sh
ENV PATH /opt/conda/bin:$PATH

RUN echo "$LOG_TAG Install python related packages" && \
    apt-get -y update && \
    apt-get install -y python-dev python-pip && \
    apt-get install -y gfortran && \
    # numerical/algebra packages
    apt-get install -y libblas-dev libatlas-dev liblapack-dev && \
    # font, image for matplotlib
    apt-get install -y libpng-dev libfreetype6-dev libxft-dev && \
    # for tkinter
    apt-get install -y python-tk libxml2-dev libxslt-dev zlib1g-dev && \
    conda config --set always_yes yes --set changeps1 no && \
    conda update -q conda && \
    conda info -a && \
    conda config --add channels conda-forge && \
    conda install -q numpy pandas matplotlib pandasql scikit-learn ipython jupyter_client ipykernel bokeh tensorflow keras && \
    pip install -q scipy ggplot grpcio bkzep selenium bs4 scrapy networkX gensim seaborn && \
    pip install -q scipy ggplot grpcio bkzep  && \
	pip install onesaitplatform-client-services

RUN echo "$LOG_TAG Install R related packages" && \
    echo "deb http://cran.rstudio.com/bin/linux/ubuntu xenial/" | tee -a /etc/apt/sources.list && \
    gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 && \
    gpg -a --export E084DAB9 | apt-key add - && \
    apt-get -y update && \
    apt-get -y --allow-unauthenticated install r-base r-base-dev && \
    R -e "install.packages('knitr', repos='http://cran.us.r-project.org')" && \
    R -e "install.packages('ggplot2', repos='http://cran.us.r-project.org')" && \
    R -e "install.packages('googleVis', repos='http://cran.us.r-project.org')" && \
    R -e "install.packages('data.table', repos='http://cran.us.r-project.org')" && \
    # for devtools, Rcpp
    apt-get -y install libcurl4-gnutls-dev libssl-dev && \
    add-apt-repository ppa:cran/libgit2 && \
    apt-get update && \
    apt-get -y install libssh2-1-dev libgit2-dev && \
    R -e "install.packages('gert')" && \
    R -e "install.packages('devtools', repos='http://cran.us.r-project.org')" && \
    R -e "install.packages('Rcpp', repos='http://cran.us.r-project.org')" && \
    Rscript -e "library('devtools'); library('Rcpp'); install_github('ramnathv/rCharts')"

ENV GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=graylog \
    GRAYLOG_PORT=12201 \
    GRAYLOG_APP_NAME=Zeppelin-server-1
    
RUN echo "$LOG_TAG Download Zeppelin binary" && \
    wget -O /tmp/zeppelin-${Z_VERSION}-bin-all.tgz http://archive.apache.org/dist/zeppelin/zeppelin-${Z_VERSION}/zeppelin-${Z_VERSION}-bin-all.tgz && \
    tar -zxvf /tmp/zeppelin-${Z_VERSION}-bin-all.tgz && \
    rm -rf /tmp/zeppelin-${Z_VERSION}-bin-all.tgz && \
    mv /zeppelin-${Z_VERSION}-bin-all ${Z_HOME} && \
    echo "$LOG_TAG Download Graylog appender" && \
    curl -L -X GET  https://search.maven.org/classic/remotecontent?filepath=biz/paluch/logging/logstash-gelf/1.14.1/logstash-gelf-1.14.1.jar --output ${Z_HOME}/lib/logstash-gelf-1.14.1.jar

RUN echo "$LOG_TAG Cleanup" && \
    apt-get autoclean && \
    apt-get clean

# Onesait Properties -----------------------------
# Create the notebook user and group
RUN groupadd -r notebook -g 433 && useradd -u 431 -r -g notebook -d /opt/notebook -s /sbin/nologin -c "Notebook user" notebook

# OCP anonymous user
RUN chgrp root /etc/passwd && chmod ug+rw /etc/passwd

COPY shiro.ini ${Z_HOME}/conf/
# Bug in zeppelin (search in /zeppelin for zeppelin-site.xml)
#COPY zeppelin-site.xml ${Z_HOME}/conf/ 
COPY zeppelin-site.xml ${Z_HOME} 
COPY zeppelin-onesait-platform*.jar ${Z_HOME}/interpreter/onesaitplatform/  
COPY zeppelin-onesait-platform*.jar ${Z_HOME}/lib/interpreter/
COPY authenticator*.jar ${Z_HOME}/lib/
COPY zeppelin.sh ${Z_HOME}/bin/    
COPY log4j.properties ${Z_HOME}/conf/
COPY log4j-graylog.properties ${Z_HOME}/conf/

RUN chmod 777 ${Z_HOME}/bin/zeppelin.sh

RUN echo "$LOG_TAG Download Spark binary" && \
    #wget -O /tmp/spark-2.4.5-bin-hadoop2.7.tgz http://apache.uvigo.es/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz && 
    wget -O /tmp/spark-2.4.5-bin-hadoop2.7.tgz https://archive.apache.org/dist/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz && \ 
    #try at https://archive.apache.org/dist/spark/spark-2.4.5/
    tar -zxvf /tmp/spark-2.4.5-bin-hadoop2.7.tgz && \
    rm -rf /tmp/spark-2.4.5-bin-hadoop2.7.tgz 
	
ENV SPARK_HOME=/spark-2.4.5-bin-hadoop2.7
    
RUN apt-get -y update && \
	apt-get install -y libmysqlclient-dev    

RUN cp -r /zeppelin/conf /tmp && \
    chmod -R 777 /tmp/conf && \
    chown -R notebook:notebook /tmp/conf && \
    chmod -R 777 ${Z_HOME} && \
    chown -R notebook:notebook ${Z_HOME}
# ------------------------------------------------

EXPOSE 8080

USER notebook

ENTRYPOINT [ "/usr/bin/tini", "--" ]
WORKDIR ${Z_HOME}
CMD ["bin/zeppelin.sh"]
