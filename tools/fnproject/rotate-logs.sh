while true; do /usr/sbin/logrotate /etc/logrotate.d/fnproject --state /app/logs/fn-state; sleep 10m; done &
