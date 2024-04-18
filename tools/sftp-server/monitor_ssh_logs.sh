#!/bin/bash

path="/etc/ssh"
fifoFile="$path/ssh_fifo"

mkfifo $fifoFile

## Monitor the FIFO file and store the SSHD logs
while true
do
    if read line; then
       printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$line" >> "$path/sshd_debug.log"
    fi
done <"$fifoFile"
