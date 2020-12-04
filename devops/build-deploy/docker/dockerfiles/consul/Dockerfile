FROM consul:latest
FROM envoyproxy/envoy:v1.8.0
COPY --from=0 /bin/consul /bin/consul
ADD entrypoint.sh /entrypoint.sh
RUN apt-get update &&\
    apt-get --assume-yes install curl
ENTRYPOINT ["/entrypoint.sh"]