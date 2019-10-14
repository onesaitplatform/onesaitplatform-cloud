#!/bin/bash

echo "              _                                        __ ";
echo "  _ __   __ _(_)_ __ __  __  _ __ ___  ___ ___  _ __  / _|";
echo " | '_ \ / _\` | | '_ \\ \/ / | '__/ _ \/ __/ _ \| '_ \| |_ ";
echo " | | | | (_| | | | | |>  <  | | |  __/ (_| (_) | | | |  _|";
echo " |_| |_|\__, |_|_| |_/_/\_\ |_|  \___|\___\___/|_| |_|_|  ";
echo " | |__  |___/     / \  _   _| |_    |___ \                ";
echo " | '_ \| | | |   / _ \| | | | __|____ __) |               ";
echo " | |_) | |_| |  / ___ \ |_| | ||_____/ __/                ";
echo " |_.__/ \__, | /_/   \_\__,_|\__|   |_____|               ";
echo "        |___/                                             ";
echo "{petrigilf,kristof.pap,paraire}@gmail.com"

echo ""
/usr/sbin/nginx -V

echo ""
echo "Starting nginx"
/usr/sbin/nginx -c /etc/nginx/nginx.conf

if [ $? -eq 0 ]; then
    echo "   [*] nginx started."
else
    echo "   [!] nginx failed to start, please review your configuration."
fi

echo ""
echo "Starting Gunicorn"
gunicorn -b0.0.0.0:8000 reconf.wsgi