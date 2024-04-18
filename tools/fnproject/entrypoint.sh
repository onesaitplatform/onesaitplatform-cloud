#!/bin/sh
exec /app/rotate-logs.sh &
exec /app/fnserver &> /app/logs/fnproject.log
