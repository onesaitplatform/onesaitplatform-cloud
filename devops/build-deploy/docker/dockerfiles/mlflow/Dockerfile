FROM python:3.9.5

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
      module.name="mlflow" \
      module.mlflow.version="1.19.0"

ENV MYSQLCLIENT_VERSION 2.0.3
ENV MLFLOW_VERSION 1.19.0
ENV SERVER_PORT 5000
ENV SERVER_HOST 0.0.0.0

RUN pip install mysqlclient==${MYSQLCLIENT_VERSION} && \
    pip install mlflow==${MLFLOW_VERSION} && \
    pip install mlflow-onesaitplatform-plugin==0.2.9

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x docker-entrypoint.sh

WORKDIR /

ENTRYPOINT ["./docker-entrypoint.sh"]
