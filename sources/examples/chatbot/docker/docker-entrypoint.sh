#!/bin/sh

python REST.py & java -Dspring.profiles.active=docker -jar /app.jar
