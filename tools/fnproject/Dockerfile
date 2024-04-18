# build stage
FROM fnproject/fnserver
RUN mkdir logs
RUN apk update
RUN apk add tzdata logrotate
COPY fnproject /etc/logrotate.d/fnproject
COPY rotate-logs.sh rotate-logs.sh
ENV TZ=Europe/Paris
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh
RUN chmod +x rotate-logs.sh
CMD ["/app/entrypoint.sh"]
EXPOSE 8080
