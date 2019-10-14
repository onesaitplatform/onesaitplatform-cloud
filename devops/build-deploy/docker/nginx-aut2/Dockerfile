FROM python:3

ENV PYTHONUNBUFFERED 1

RUN apt update
RUN apt install nginx -y

RUN mkdir /app
WORKDIR /app

ADD scripts/requiements.pip /app/
RUN pip3 install -r requiements.pip

ADD scripts/startServer.sh /app/
RUN chmod 755 /app/startServer.sh

ADD src/reconf/reconf /app/reconf
ADD src/reconf/api /app/api
ADD src/reconf/manage.py /app/

EXPOSE 90 8000
